package ge.freeuni.informatics.acceptance.mock;

import ge.freeuni.informatics.common.events.SubmissionEvent;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.customtest.CustomTestRun;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;
import ge.freeuni.informatics.common.model.submission.SubmissionTestResult;
import ge.freeuni.informatics.common.model.submission.TestStatus;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.Testcase;
import ge.freeuni.informatics.judgeintegration.IJudgeIntegration;
import ge.freeuni.informatics.repository.submission.SubmissionJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

// @Component - Disabled: Using real JudgeIntegration with MockKafkaWorker instead
// @Primary
public class MockJudgeIntegration implements IJudgeIntegration {

    private static final Logger log = LoggerFactory.getLogger(MockJudgeIntegration.class);

    @Autowired
    private SubmissionJpaRepository submissionRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final Map<Long, SubmissionResult> predefinedResults = new ConcurrentHashMap<>();
    private Function<Submission, SubmissionResult> defaultResultProvider = this::defaultResult;

    public void setPredefinedResult(Long taskId, SubmissionResult result) {
        predefinedResults.put(taskId, result);
    }

    public void setDefaultResultProvider(Function<Submission, SubmissionResult> provider) {
        this.defaultResultProvider = provider;
    }

    public void clearPredefinedResults() {
        predefinedResults.clear();
        defaultResultProvider = this::defaultResult;
    }

    @Override
    @Async
    @Transactional
    public void addSubmission(Task task, Submission submission) throws InformaticsServerException {
        log.info("=== MOCK JUDGE START === Submission ID: {}, Task ID: {}, Thread: {}", 
                submission.getId(), task.getId(), Thread.currentThread().getName());

        SubmissionResult result = predefinedResults.getOrDefault(task.getId(), defaultResultProvider.apply(submission));
        log.debug("Using result: correct={}, status={}", result.isCorrect(), result.getStatus());

        submission.setStatus(SubmissionStatus.COMPILING);
        submissionRepository.save(submission);
        log.debug("Submission {} status changed to COMPILING", submission.getId());

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Mock judge interrupted for submission {}", submission.getId());
        }

        submission.setStatus(SubmissionStatus.RUNNING);
        submissionRepository.save(submission);
        log.debug("Submission {} status changed to RUNNING", submission.getId());

        List<SubmissionTestResult> testResults = new ArrayList<>();
        List<Testcase> testcases = task.getTestcases();

        if (testcases != null && !testcases.isEmpty()) {
            for (int i = 0; i < testcases.size(); i++) {
                Testcase testcase = testcases.get(i);
                SubmissionTestResult testResult = new SubmissionTestResult();
                testResult.setTestKey(testcase.getKey());

                float testScore = result.getTestScores() != null && i < result.getTestScores().length
                        ? result.getTestScores()[i]
                        : (result.isCorrect() ? 1.0f : 0.0f);

                testResult.setScore(testScore);
                testResult.setTestStatus(testScore >= 1.0f ? TestStatus.CORRECT : TestStatus.WRONG_ANSWER);
                testResult.setTime(100);
                testResult.setMemory(1024);
                testResults.add(testResult);
            }
        }

        submission.setSubmissionTestResults(testResults);

        float totalScore = task.getTaskScoreType().evaluate(testResults, task.getTaskScoreParameter());
        submission.setScore(totalScore);

        if (result.getStatus() != null) {
            submission.setStatus(result.getStatus());
        } else if (totalScore >= 100.0f) {
            submission.setStatus(SubmissionStatus.CORRECT);
        } else if (totalScore > 0) {
            submission.setStatus(SubmissionStatus.PARTIAL);
        } else {
            submission.setStatus(SubmissionStatus.FAILED);
        }

        submissionRepository.save(submission);
        log.info("=== MOCK JUDGE COMPLETE === Submission ID: {}, Score: {}, Status: {}, Thread: {}", 
                submission.getId(), totalScore, submission.getStatus(), Thread.currentThread().getName());

        eventPublisher.publishEvent(new SubmissionEvent(submission));
        log.debug("Published SubmissionEvent for submission {}", submission.getId());
    }

    @Override
    public void addCustomTest(Task task, CustomTestRun run, CodeLanguage language) throws InformaticsServerException {

    }

    private SubmissionResult defaultResult(Submission submission) {
        return SubmissionResult.correct();
    }

    public static class SubmissionResult {
        private final boolean correct;
        private final float[] testScores;
        private final SubmissionStatus status;

        private SubmissionResult(boolean correct, float[] testScores, SubmissionStatus status) {
            this.correct = correct;
            this.testScores = testScores;
            this.status = status;
        }

        public static SubmissionResult correct() {
            return new SubmissionResult(true, null, SubmissionStatus.CORRECT);
        }

        public static SubmissionResult wrong() {
            return new SubmissionResult(false, null, SubmissionStatus.WRONG_ANSWER);
        }

        public static SubmissionResult partial(float... testScores) {
            return new SubmissionResult(false, testScores, SubmissionStatus.PARTIAL);
        }

        public static SubmissionResult withScores(float... testScores) {
            boolean allCorrect = true;
            for (float score : testScores) {
                if (score < 1.0f) {
                    allCorrect = false;
                    break;
                }
            }
            return new SubmissionResult(allCorrect, testScores, null);
        }

        public static SubmissionResult compilationError() {
            return new SubmissionResult(false, null, SubmissionStatus.COMPILATION_ERROR);
        }

        public static SubmissionResult runtimeError() {
            return new SubmissionResult(false, null, SubmissionStatus.RUNTIME_ERROR);
        }

        public static SubmissionResult timeLimitExceeded() {
            return new SubmissionResult(false, null, SubmissionStatus.TIME_LIMIT_EXCEEDED);
        }

        public boolean isCorrect() {
            return correct;
        }

        public float[] getTestScores() {
            return testScores;
        }

        public SubmissionStatus getStatus() {
            return status;
        }
    }
}
