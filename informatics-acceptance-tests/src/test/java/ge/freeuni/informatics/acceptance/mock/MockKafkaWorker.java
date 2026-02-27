package ge.freeuni.informatics.acceptance.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import ge.freeuni.informatics.common.model.submission.TestStatus;
import ge.freeuni.informatics.judgeintegration.model.CallbackType;
import ge.freeuni.informatics.judgeintegration.model.KafkaCallback;
import ge.freeuni.informatics.judgeintegration.model.KafkaTask;
import ge.freeuni.informatics.judgeintegration.model.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock worker that simulates the real worker's Kafka-based submission processing.
 * Listens to submission-topic and sends callbacks to submission-callback.
 */
@Component
public class MockKafkaWorker {
    
    private static final Logger log = LoggerFactory.getLogger(MockKafkaWorker.class);
    
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    // Map of submission ID -> desired score percentage (0-100)
    private final Map<Long, Integer> submissionScores = new ConcurrentHashMap<>();
    
    // Map of submission ID -> desired test status (for special cases like TLE, CE, etc.)
    private final Map<Long, TestStatus> submissionTestStatus = new ConcurrentHashMap<>();
    
    // Map of submission ID -> should fail compilation
    private final Map<Long, Boolean> submissionCompilationFailure = new ConcurrentHashMap<>();
    
    public MockKafkaWorker(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Set the desired score for a submission.
     * @param submissionId The submission ID
     * @param scorePercentage The desired score (0-100)
     */
    public void setSubmissionScore(Long submissionId, int scorePercentage) {
        submissionScores.put(submissionId, scorePercentage);
        log.info("Set mock score for submission {}: {}%", submissionId, scorePercentage);
    }
    
    /**
     * Set the submission to fail with a compilation error.
     * @param submissionId The submission ID
     */
    public void setCompilationError(Long submissionId) {
        submissionCompilationFailure.put(submissionId, true);
        log.info("Set compilation error for submission {}", submissionId);
    }
    
    /**
     * Set the submission to fail with a specific test status (e.g., TIME_LIMIT_EXCEEDED).
     * @param submissionId The submission ID
     * @param status The test status to return
     */
    public void setTestStatus(Long submissionId, TestStatus status) {
        submissionTestStatus.put(submissionId, status);
        log.info("Set test status for submission {}: {}", submissionId, status);
    }
    
    /**
     * Clear all predefined scores and statuses.
     */
    public void clearScores() {
        submissionScores.clear();
        submissionTestStatus.clear();
        submissionCompilationFailure.clear();
        log.info("Cleared all mock submission scores and statuses");
    }
    
    @KafkaListener(topics = "submission-topic", groupId = "test-worker", concurrency = "3")
    public void processSubmission(String message) {
        try {
            KafkaTask task = objectMapper.readValue(message, KafkaTask.class);
            log.info("=== MOCK WORKER === Received task: submissionId={}, stage={}, testcaseKey={}", 
                    task.submissionId(), task.stage(), task.testId());
            
            if (task.stage() == Stage.COMPILATION) {
                handleCompilation(task);
            } else if (task.stage() == Stage.TESTING) {
                handleTesting(task);
            }
            
        } catch (Exception e) {
            log.error("Error processing submission task", e);
        }
    }
    
    private void handleCompilation(KafkaTask task) {
        try {
            Long submissionId = Long.parseLong(task.submissionId());
            
            // Send compilation started
            sendCallback(new KafkaCallback(
                    submissionId,
                    CallbackType.COMPILATION_STARTED,
                    null, null, null, null, null, null, null, null
            ));
            
            // Simulate compilation time
            Thread.sleep(100);
            
            // Check if this submission should fail compilation
            if (submissionCompilationFailure.getOrDefault(submissionId, false)) {
                sendCallback(new KafkaCallback(
                        submissionId,
                        CallbackType.COMPILATION_FAILED,
                        null, 
                        "error: expected ';' before '}' token", // compilation error message
                        null, null, null, null, null, null
                ));
                log.info("=== MOCK WORKER === Compilation failed for submission {}", submissionId);
            } else {
                // Send compilation completed (successful)
                sendCallback(new KafkaCallback(
                        submissionId,
                        CallbackType.COMPILATION_COMPLETED,
                        null, null, null, null, null, null, null, null
                ));
                log.info("=== MOCK WORKER === Compilation completed for submission {}", submissionId);
            }
            
        } catch (Exception e) {
            log.error("Error handling compilation", e);
        }
    }
    
    private void handleTesting(KafkaTask task) {
        try {
            Long submissionId = Long.parseLong(task.submissionId());
            String testcaseKey = task.testId();
            
            // Simulate test execution time
            Thread.sleep(50);
            
            // Check if a specific test status is set for this submission
            TestStatus forcedStatus = submissionTestStatus.get(submissionId);
            
            // Determine test result
            TestStatus status;
            double score;
            String message;
            long timeMs = 50L;
            
            if (forcedStatus != null) {
                // Use the forced status (e.g., TIME_LIMIT_EXCEEDED)
                status = forcedStatus;
                score = 0.0;
                
                switch (forcedStatus) {
                    case TIME_LIMIT_EXCEEDED:
                        message = "Time Limit Exceeded";
                        timeMs = 2000L; // Simulated excessive time
                        break;
                    case RUNTIME_ERROR:
                        message = "Runtime Error";
                        break;
                    case MEMORY_LIMIT_EXCEEDED:
                        message = "Memory Limit Exceeded";
                        break;
                    default:
                        message = forcedStatus.toString();
                        break;
                }
            } else {
                // Get the desired score percentage for this submission (default 100%)
                int scorePercentage = submissionScores.getOrDefault(submissionId, 100);
                
                // Parse testcase key (e.g., "1", "2", ..., "10")
                int testNumber = Integer.parseInt(testcaseKey);
                
                // Calculate if this test should pass based on score percentage
                // If scorePercentage is 40, tests 1-4 pass (out of 10 tests)
                int passingTests = (int) Math.ceil(scorePercentage / 10.0);
                boolean testPasses = testNumber <= passingTests;
                
                if (testPasses) {
                    status = TestStatus.CORRECT;
                    score = 1.0; // Full score for this test
                    message = "Accepted";
                } else {
                    status = TestStatus.WRONG_ANSWER;
                    score = 0.0; // No score for this test
                    message = "Wrong Answer";
                }
            }
            
            // Send test completed callback
            sendCallback(new KafkaCallback(
                    submissionId,
                    CallbackType.TEST_COMPLETED,
                    testcaseKey,
                    message,
                    (long) (score * 100), // Convert to percentage for callback
                    status,
                    0, // exit code
                    timeMs, // time in ms
                    1024L, // memory in KB
                    "Test outcome"
            ));
            
            log.info("=== MOCK WORKER === Test {} for submission {}: {} (score: {})", 
                    testcaseKey, submissionId, status, score);
            
        } catch (Exception e) {
            log.error("Error handling testing", e);
        }
    }
    
    private void sendCallback(KafkaCallback callback) {
        try {
            String message = objectMapper.writeValueAsString(callback);
            kafkaTemplate.send("submission-callback", message);
            log.debug("Sent callback: {}", message);
        } catch (Exception e) {
            log.error("Error sending callback", e);
        }
    }
}
