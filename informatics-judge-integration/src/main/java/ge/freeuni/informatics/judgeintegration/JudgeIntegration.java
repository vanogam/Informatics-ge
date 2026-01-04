package ge.freeuni.informatics.judgeintegration;

import com.fasterxml.jackson.databind.ObjectMapper;
import ge.freeuni.informatics.common.events.SubmissionEvent;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;
import ge.freeuni.informatics.common.model.submission.SubmissionTestResult;
import ge.freeuni.informatics.common.model.task.CheckerType;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.Testcase;
import ge.freeuni.informatics.judgeintegration.model.KafkaCallback;
import ge.freeuni.informatics.judgeintegration.model.KafkaTask;
import ge.freeuni.informatics.judgeintegration.model.Stage;
import ge.freeuni.informatics.repository.submission.SubmissionJpaRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JudgeIntegration implements IJudgeIntegration{

    @Autowired
    Logger log;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private SubmissionJpaRepository submissionRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private static final HashMap<Long, TreeMap<String, Integer>> testCompletionMap = new HashMap<>();

    private static final ConcurrentHashMap<Long, Object> submissionLocks = new ConcurrentHashMap<>();

    @Override
    public void addSubmission(Task task, Submission submission) throws InformaticsServerException {
        KafkaTask kafkaTask = new KafkaTask(
                String.valueOf(task.getId()),
                String.valueOf(task.getContest().getId()),
                String.valueOf(submission.getId()),
                String.valueOf(submission.getFileName()),
                CodeLanguage.valueOf(submission.getLanguage()),
                task.getTimeLimitMillis(),
                task.getMemoryLimitMB() * 1024,
                null,
                null,
                null,
                task.getCheckerType(),
                Stage.COMPILATION
        );
        submissionLocks.put(submission.getId(), new Object());
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String message = objectMapper.writeValueAsString(kafkaTask);
            log.debug("Publishing compilation message: {}", message);
            kafkaProducerService.sendMessage("submission-topic", message);
        } catch (IOException e) {
            log.error("Failed to serialize compilation kafka message", e);
            throw new InformaticsServerException("serializationError", e);
        }
    }

    private void sendTestMessages(Task task, Submission submission) throws InformaticsServerException {
        List<Testcase> testcases = task.getTestcases().stream()
                .sorted(Comparator.comparing(Testcase::getKey))
                .toList();
        for (Testcase testcase : testcases) {
            KafkaTask kafkaTask = new KafkaTask(
                    String.valueOf(task.getId()),
                    String.valueOf(task.getContest().getId()),
                    String.valueOf(submission.getId()),
                    String.valueOf(submission.getFileName()),
                    CodeLanguage.valueOf(submission.getLanguage()),
                    task.getTimeLimitMillis(),
                    task.getMemoryLimitMB() * 1024,
                    testcase.getKey(),
                    testcase.getInputFileAddress().substring(testcase.getInputFileAddress().lastIndexOf("/") + 1),
                    testcase.getOutputFileAddress().substring(testcase.getOutputFileAddress().lastIndexOf("/") + 1),
                    task.getCheckerType(),
                    Stage.TESTING
            );
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String message = objectMapper.writeValueAsString(kafkaTask);
                log.debug("Kafka message: {}", message);
                kafkaProducerService.sendMessage("submission-topic", message);
                log.info("Sent submission to Kafka: {}", submission.getId());
            } catch (IOException e) {
                log.error("Failed to serialize KafkaTask: {}", e.getMessage());
                throw new InformaticsServerException("serializationError", e);
            }
        }
        testCompletionMap.put(submission.getId(), new TreeMap<>());
        for (int i = 0; i < task.getTestcases().size(); i++) {
            testCompletionMap.get(submission.getId()).put(task.getTestcases().get(i).getKey(), i);
        }
    }

    @KafkaListener(topics = "submission-callback", groupId = "core")
    @Transactional
    protected void listenToCompletionTopic(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            KafkaCallback callback = objectMapper.readValue(message, KafkaCallback.class);
            log.info("`Received callback for submission`: {}, {}, {}", callback.submissionId(), callback.messageType(), callback.testcaseKey());
            Submission submission = submissionRepository.getReferenceById(callback.submissionId());
            if (!submissionLocks.containsKey(submission.getId())) {
                log.warn("No lock found for submission: {}", submission.getId());
                // TODO: Add restart handling
                return;
            }
            synchronized (submissionLocks.get(submission.getId())) {
                switch (callback.messageType()) {
                    case COMPILATION_STARTED:
                        submission.setStatus(SubmissionStatus.COMPILING);
                        submissionRepository.save(submission);
                        break;
                    case COMPILATION_COMPLETED:
                        submission.setStatus(SubmissionStatus.RUNNING);
                        submission.setCurrentTest(1);
                        sendTestMessages(submission.getTask(), submission);
                        submissionRepository.save(submission);
                        break;
                    case COMPILATION_FAILED:
                        submission.setStatus(SubmissionStatus.COMPILATION_ERROR);
                        finalizeSubmission(submission, callback);
                        log.error("Compilation failed for submission: {}", submission.getId());
                        break;
                    case SYSTEM_ERROR:
                        submission.setStatus(SubmissionStatus.SYSTEM_ERROR);
                        submission.setCompilationMessage(callback.message());
                        finalizeSubmission(submission, callback);
                        log.error("System error for submission: {}, message: {}", submission.getId(), callback.message());
                        break;
                    case TEST_COMPLETED:
                        if (!testCompletionMap.get(submission.getId()).containsKey(callback.testcaseKey())) {
                            return;
                        }
                        testCompletionMap.get(submission.getId()).remove(callback.testcaseKey());
                        if (testCompletionMap.get(submission.getId()).isEmpty()) {
                            finalizeSubmission(submission, callback);
                            return;
                        }
                        submission.setCurrentTest(testCompletionMap.get(submission.getId()).firstEntry().getValue());
                        submissionRepository.save(submission);
                        log.info("Test completed for submission: {}, test case: {}", submission.getId(), callback.testcaseKey());
                        break;
                    default:
                }
            }
        } catch (Exception e) {
            log.error("Error while processing submission message", e);
            throw new RuntimeException(e);
        }
    }

    private void finalizeSubmission(Submission submission, KafkaCallback callback) {
        if (submission.getStatus() == SubmissionStatus.COMPILATION_ERROR) {
            submission.setScore(0f);
            submission.setCompilationMessage(callback.message());
        } else {
            float finalScore = submission.getSubmissionTestResults().stream().map(SubmissionTestResult::getScore).reduce(0f, (sum, result) -> sum + result);
            if (finalScore == 0f) {
                submission.setStatus(SubmissionStatus.FAILED);
            } else if (submission.getSubmissionTestResults().stream().allMatch(res -> res.getScore() == 1f)) {
                submission.setStatus(SubmissionStatus.CORRECT);
            } else {
                submission.setStatus(SubmissionStatus.PARTIAL);
            }
        }
        float finalScore = 0f;
        try {
            finalScore = submission.getTask().getTaskScoreType().evaluate(submission.getSubmissionTestResults(),
                    submission.getTask().getTaskScoreParameter());
        } catch (Exception e) {
            log.error("Error evaluating task score for submission: {}", submission.getId(), e);
            submission.setStatus(SubmissionStatus.SYSTEM_ERROR);
        }
        submission.setScore(finalScore);
        submissionRepository.save(submission);
        testCompletionMap.remove(submission.getId());
        submissionLocks.remove(submission.getId());
        log.info("Submission {} finalized with status: {}", submission.getId(), submission.getStatus());

        eventPublisher.publishEvent(new SubmissionEvent(submission));
    }
}
