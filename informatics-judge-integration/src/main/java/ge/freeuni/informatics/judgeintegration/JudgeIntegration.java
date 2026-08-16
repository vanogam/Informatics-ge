package ge.freeuni.informatics.judgeintegration;

import com.fasterxml.jackson.databind.ObjectMapper;
import ge.freeuni.informatics.common.events.SubmissionEvent;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.customtest.CustomTestRun;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;
import ge.freeuni.informatics.common.model.submission.SubmissionTestResult;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TaskScoreType;
import ge.freeuni.informatics.common.model.task.TestKeys;
import ge.freeuni.informatics.common.model.task.Testcase;
import ge.freeuni.informatics.judgeintegration.model.KafkaCallback;
import ge.freeuni.informatics.judgeintegration.model.KafkaTask;
import ge.freeuni.informatics.judgeintegration.model.Stage;
import ge.freeuni.informatics.repository.submission.SubmissionJpaRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
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

    private static final int COMPILATION_MESSAGE_MAX_LENGTH = 1000;
    private static final int TEST_RESULT_MESSAGE_MAX_LENGTH = 1000;
    private static final int TEST_RESULT_OUTCOME_MAX_LENGTH = 1000;
    private static final String TRUNCATION_SUFFIX = "...";

    /**
     * Statuses that mean judging has not reached a conclusion. A submission left in one of these
     * across a restart has no in-memory tracking left and would otherwise sit there forever.
     */
    private static final List<SubmissionStatus> IN_FLIGHT_STATUSES = List.of(
            SubmissionStatus.IN_QUEUE, SubmissionStatus.COMPILING, SubmissionStatus.RUNNING);

    /**
     * Beyond this age an in-flight submission is failed rather than re-judged, so a long-forgotten
     * submission cannot silently re-score a finished contest.
     */
    @Value("${ge.freeuni.informatics.judge.recovery.maxAgeHours:24}")
    private long recoveryMaxAgeHours;

    @Value("${ge.freeuni.informatics.judge.recovery.enabled:true}")
    private boolean recoveryEnabled;

    private String truncateToLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        if (maxLength <= TRUNCATION_SUFFIX.length()) {
            return value.substring(0, maxLength);
        }
        return value.substring(0, maxLength - TRUNCATION_SUFFIX.length()) + TRUNCATION_SUFFIX;
    }

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
                task.getTaskType(),
                numProcesses(task),
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

    @Override
    public void addCustomTest(Task task, CustomTestRun run, CodeLanguage language) throws InformaticsServerException {
        String contestId = task.getContest() != null ? String.valueOf(task.getContest().getId()) : "0";

        KafkaTask kafkaTask = new KafkaTask(
                String.valueOf(task.getId()),
                contestId,
                String.valueOf(run.getId()),
                run.getSubmissionFile(),
                language,
                task.getTimeLimitMillis(),
                task.getMemoryLimitMB() * 1024,
                null,
                null,
                null,
                task.getCheckerType(),
                task.getTaskType(),
                numProcesses(task),
                Stage.COMPILATION
        );
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String message = objectMapper.writeValueAsString(kafkaTask);
            log.debug("Publishing custom test compilation message: {}", message);
            kafkaProducerService.sendMessage("submission-topic", message);
        } catch (IOException e) {
            log.error("Failed to serialize custom test compilation kafka message", e);
            throw new InformaticsServerException("serializationError", e);
        }
    }

    private void sendTestMessages(Task task, Submission submission) throws InformaticsServerException {
        sendTestMessages(task, submission, null);
    }

    /**
     * @param only test keys to publish, or null for every test of the task
     */
    private void sendTestMessages(Task task, Submission submission, Set<String> only)
            throws InformaticsServerException {
        List<Testcase> testcases = task.getTestcases().stream()
                .sorted(Comparator.comparing(Testcase::getKey))
                .filter(tc -> only == null || only.contains(tc.getKey()))
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
                    task.getTaskType(),
                    numProcesses(task),
                    Stage.TESTING
            );
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String message = objectMapper.writeValueAsString(kafkaTask);
                log.debug("Kafka message: {}", message);
                kafkaProducerService.sendMessage("submission-topic",
                        submission.getId() + ":" + testcase.getKey(), message);
                log.info("Sent submission to Kafka: {}", submission.getId());
            } catch (IOException e) {
                log.error("Failed to serialize KafkaTask: {}", e.getMessage());
                throw new InformaticsServerException("serializationError", e);
            }
        }
        if (only == null) {
            testCompletionMap.put(submission.getId(), outstandingTests(submission));
        }
    }

    @KafkaListener(topics = "submission-callback", groupId = "core")
    @Transactional
    protected void listenToCompletionTopic(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            KafkaCallback callback = objectMapper.readValue(message, KafkaCallback.class);
            log.info("`Received callback for submission`: {}, {}, {}", callback.submissionId(), callback.messageType(), callback.testcaseKey());

            // If this callback is not for a regular submission (e.g., custom test run),
            // ignore it here and let other listeners handle it.
            Submission submission = submissionRepository.findById(callback.submissionId()).orElse(null);
            if (submission == null) {
                log.info("No Submission entity found for id {}, skipping in JudgeIntegration", callback.submissionId());
                return;
            }
            if (!submissionLocks.containsKey(submission.getId())) {
                // Tracking lives in memory and is lost on restart. Rebuild it from the database
                // rather than dropping the result the worker just spent time producing.
                if (!restoreTracking(submission)) {
                    return;
                }
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
                        submission.setSubmissionTestResults(new java.util.ArrayList<>());
                        submission.setCompilationMessage(truncateToLength(callback.message(), COMPILATION_MESSAGE_MAX_LENGTH));
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
                        submission.setCompilationMessage(truncateToLength(callback.message(), COMPILATION_MESSAGE_MAX_LENGTH));
                        finalizeSubmission(submission, callback);
                        log.error("System error for submission: {}, message: {}", submission.getId(), callback.message());
                        break;
                    case TEST_COMPLETED:
                        if (!testCompletionMap.get(submission.getId()).containsKey(callback.testcaseKey())) {
                            return;
                        }
                        
                        // Create and store test result
                        SubmissionTestResult testResult = createTestResult(callback);
                        if (submission.getSubmissionTestResults() == null) {
                            submission.setSubmissionTestResults(new ArrayList<>());
                        }
                        submission.getSubmissionTestResults().add(testResult);
                        
                        testCompletionMap.get(submission.getId()).remove(callback.testcaseKey());
                        if (testCompletionMap.get(submission.getId()).isEmpty()) {
                            finalizeSubmission(submission, callback);
                            return;
                        }
                        submission.setCurrentTest(testCompletionMap.get(submission.getId()).firstEntry().getValue());
                        submissionRepository.save(submission);
                        log.info("Test completed for submission: {}, test case: {}, score: {}, status: {}", 
                                submission.getId(), callback.testcaseKey(), testResult.getScore(), testResult.getTestStatus());
                        break;
                    default:
                }
            }
        } catch (Exception e) {
            log.error("Error while processing submission message", e);
            throw new RuntimeException(e);
        }
    }

    private int numProcesses(Task task) {
        return task.getNumProcesses() == null ? 1 : task.getNumProcesses();
    }


    /**
     * Recreates the in-memory tracking for a submission whose judging predates a restart.
     *
     * @return false when the submission is already finished, so the callback should be ignored
     */
    private boolean restoreTracking(Submission submission) {
        if (!IN_FLIGHT_STATUSES.contains(submission.getStatus())) {
            log.debug("Ignoring callback for already finished submission {}", submission.getId());
            return false;
        }
        submissionLocks.putIfAbsent(submission.getId(), new Object());
        testCompletionMap.put(submission.getId(), outstandingTests(submission));
        log.info("Restored tracking for submission {} after restart, {} test(s) outstanding",
                submission.getId(), testCompletionMap.get(submission.getId()).size());
        return true;
    }

    /**
     * Tests that still owe a result: every testcase of the task minus those already recorded.
     * Judging progress is therefore derivable from the database and never has to be persisted
     * separately. Values are the index used to report which test is currently running.
     */
    private TreeMap<String, Integer> outstandingTests(Submission submission) {
        TreeMap<String, Integer> outstanding = new TreeMap<>();
        Task task = submission.getTask();
        if (task == null || task.getTestcases() == null) {
            return outstanding;
        }
        Set<String> completed = new HashSet<>();
        if (submission.getSubmissionTestResults() != null) {
            submission.getSubmissionTestResults().stream()
                    .map(SubmissionTestResult::getTestKey)
                    .forEach(completed::add);
        }
        List<Testcase> ordered = task.getTestcases().stream()
                .sorted(Comparator.comparing(Testcase::getKey))
                .toList();
        for (int i = 0; i < ordered.size(); i++) {
            String key = ordered.get(i).getKey();
            if (!completed.contains(key)) {
                outstanding.put(key, i);
            }
        }
        return outstanding;
    }

    /**
     * Re-drives submissions that were mid-judgement when the application last stopped. Without
     * this they keep their in-flight status forever: the worker's callbacks are long gone and
     * nothing else ever revisits the row.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void recoverInFlightSubmissions() {
        if (!recoveryEnabled) {
            log.info("Submission recovery disabled, skipping");
            return;
        }
        List<Submission> inFlight = submissionRepository.getAllByStatusIn(IN_FLIGHT_STATUSES);
        if (inFlight.isEmpty()) {
            return;
        }
        log.info("Found {} in-flight submission(s) to recover", inFlight.size());
        long cutoff = System.currentTimeMillis() - recoveryMaxAgeHours * 3600_000L;

        for (Submission submission : inFlight) {
            try {
                recoverSubmission(submission, cutoff);
            } catch (Exception e) {
                log.error("Failed to recover submission {}", submission.getId(), e);
            }
        }
    }

    private void recoverSubmission(Submission submission, long cutoff) throws InformaticsServerException {
        Date submittedAt = submission.getSubmissionTime();
        if (submittedAt != null && submittedAt.getTime() < cutoff) {
            log.warn("Submission {} is older than {}h, failing it instead of re-judging",
                    submission.getId(), recoveryMaxAgeHours);
            submission.setStatus(SubmissionStatus.SYSTEM_ERROR);
            submission.setCompilationMessage("Judging was interrupted and could not be resumed");
            submission.setScore(0f);
            submissionRepository.save(submission);
            return;
        }

        TreeMap<String, Integer> outstanding = outstandingTests(submission);
        submissionLocks.putIfAbsent(submission.getId(), new Object());

        if (submission.getStatus() == SubmissionStatus.RUNNING && !outstanding.isEmpty()) {
            // It compiled, so only the tests that never reported back need re-running.
            log.info("Resuming submission {}: re-queueing {} outstanding test(s)",
                    submission.getId(), outstanding.size());
            testCompletionMap.put(submission.getId(), outstanding);
            sendTestMessages(submission.getTask(), submission, outstanding.keySet());
            return;
        }
        if (submission.getStatus() == SubmissionStatus.RUNNING) {
            // Every test reported; it just never got finalised.
            log.info("Finalising submission {}: all tests already have results", submission.getId());
            testCompletionMap.put(submission.getId(), outstanding);
            finalizeSubmission(submission, submission.getCompilationMessage());
            return;
        }
        // Never got past compilation - start the whole job again.
        log.info("Re-queueing submission {} from compilation", submission.getId());
        submission.setStatus(SubmissionStatus.IN_QUEUE);
        submission.setSubmissionTestResults(new ArrayList<>());
        submissionRepository.save(submission);
        addSubmission(submission.getTask(), submission);
    }


    /**
     * Rounds the total. Each subtask has already been rounded as it was awarded (see
     * {@link TaskScoreType#roundScore}); this clears any residue from summing those.
     */
    static float roundScore(float score) {
        return TaskScoreType.roundScore(score);
    }

    private SubmissionTestResult createTestResult(KafkaCallback callback) {
        SubmissionTestResult testResult = new SubmissionTestResult();
        testResult.setTestKey(callback.testcaseKey());
        testResult.setTestStatus(callback.status());
        testResult.setMessage(truncateToLength(callback.message(), TEST_RESULT_MESSAGE_MAX_LENGTH));
        testResult.setOutcome(truncateToLength(callback.outcome(), TEST_RESULT_OUTCOME_MAX_LENGTH));
        
        // The worker always reports a fraction in [0, 1]; partial scores must survive intact.
        testResult.setScore(callback.score() == null ? 0.0f : callback.score().floatValue());
        
        // Convert time and memory from Long to Integer
        if (callback.timeMillis() != null) {
            testResult.setTime(callback.timeMillis().intValue());
        }
        if (callback.memoryKB() != null) {
            testResult.setMemory(callback.memoryKB().intValue());
        }
        
        return testResult;
    }

    private void finalizeSubmission(Submission submission, KafkaCallback callback) {
        finalizeSubmission(submission, callback == null ? null : callback.message());
    }

    private void finalizeSubmission(Submission submission, String message) {
        if (submission.getStatus() == SubmissionStatus.COMPILATION_ERROR) {
            submission.setScore(0f);
            submission.setCompilationMessage(truncateToLength(message, COMPILATION_MESSAGE_MAX_LENGTH));
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
            // GROUP_MIN slices this list positionally, and results arrive in whatever order the
            // workers finish - which, now that tests are spread across partitions, is arbitrary.
            // Sorting here is what aligns each slice with the subtask it was configured for.
            submission.getSubmissionTestResults()
                    .sort(Comparator.comparing(SubmissionTestResult::getTestKey, TestKeys.NATURAL_ORDER));
            finalScore = submission.getTask().getTaskScoreType().evaluate(submission.getSubmissionTestResults(),
                    submission.getTask().getTaskScoreParameter());
        } catch (Exception e) {
            log.error("Error evaluating task score for submission: {}", submission.getId(), e);
            submission.setStatus(SubmissionStatus.SYSTEM_ERROR);
        }
        submission.setScore(roundScore(finalScore));
        submissionRepository.save(submission);
        testCompletionMap.remove(submission.getId());
        submissionLocks.remove(submission.getId());
        log.info("Submission {} finalized with status: {}", submission.getId(), submission.getStatus());

        eventPublisher.publishEvent(new SubmissionEvent(submission));
    }
}
