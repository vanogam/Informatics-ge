package ge.freeuni.informatics.judgeintegration;

import com.fasterxml.jackson.databind.ObjectMapper;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.customtest.CustomTestRun;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.judgeintegration.model.KafkaCallback;
import ge.freeuni.informatics.judgeintegration.model.KafkaTask;
import ge.freeuni.informatics.judgeintegration.model.Stage;
import ge.freeuni.informatics.repository.customtest.CustomTestRunRepository;
import ge.freeuni.informatics.repository.task.TaskRepository;
import org.slf4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomTestCallbackListener {

    private static final String CUSTOM_TASK_CODE = "_customTest";

    private final Logger log;
    private final CustomTestRunRepository runRepository;
    private final TaskRepository taskRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomTestCallbackListener(Logger log,
                                      CustomTestRunRepository runRepository,
                                      TaskRepository taskRepository,
                                      KafkaProducerService kafkaProducerService) {
        this.log = log;
        this.runRepository = runRepository;
        this.taskRepository = taskRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    private Task getCustomTask() {
        Optional<Task> taskOpt = taskRepository.findAll()
                .stream()
                .filter(t -> CUSTOM_TASK_CODE.equals(t.getCode()))
                .findFirst();
        return taskOpt.orElse(null);
    }

    @KafkaListener(topics = "submission-callback", groupId = "custom-tests")
    @Transactional
    public void listen(String message) {
        try {
            KafkaCallback callback = objectMapper.readValue(message, KafkaCallback.class);
            Long runId = callback.submissionId();

            CustomTestRun run = runRepository.findById(runId).orElse(null);
            if (run == null) {
                // Not a custom test run
                return;
            }

            log.info("Handling custom test callback: runId={}, type={}", runId, callback.messageType());

            switch (callback.messageType()) {
                case COMPILATION_STARTED -> {
                    run.setStatus("COMPILING");
                    runRepository.save(run);
                }
                case COMPILATION_COMPLETED -> {
                    run.setStatus("RUNNING");
                    runRepository.save(run);
                    sendSingleTestMessage(run);
                }
                case COMPILATION_FAILED -> {
                    run.setStatus("COMPILATION_ERROR");
                    run.setMessage(callback.message());
                    runRepository.save(run);
                }
                case SYSTEM_ERROR -> {
                    run.setStatus("SYSTEM_ERROR");
                    run.setMessage(callback.message());
                    runRepository.save(run);
                }
                case TEST_COMPLETED -> {
                    run.setStatus("FINISHED");
                    if (callback.timeMillis() != null) {
                        run.setTimeMillis(callback.timeMillis().intValue());
                    }
                    if (callback.memoryKB() != null) {
                        run.setMemoryKb(callback.memoryKB().intValue());
                    }
                    run.setMessage(callback.message());
                    run.setOutcome(callback.outcome());
                    runRepository.save(run);
                }
                default -> {
                }
            }
        } catch (Exception e) {
            log.error("Error while processing custom test callback", e);
        }
    }

    private void sendSingleTestMessage(CustomTestRun run) {
        Task task = getCustomTask();
        if (task == null) {
            log.error("Custom test task with code {} not found, cannot send test message", CUSTOM_TASK_CODE);
            return;
        }

        String contestId = task.getContest() != null ? String.valueOf(task.getContest().getId()) : "0";

        try {
            KafkaTask kafkaTask = new KafkaTask(
                    String.valueOf(task.getId()),
                    contestId,
                    String.valueOf(run.getId()),
                    run.getSubmissionFile(),
                    CodeLanguage.valueOf(run.getLanguage()),
                    task.getTimeLimitMillis(),
                    task.getMemoryLimitMB() * 1024,
                    "custom",
                    run.getInputFile(),
                    run.getOutputFile(),
                    task.getCheckerType(),
                    Stage.TESTING
            );
            String msg = objectMapper.writeValueAsString(kafkaTask);
            log.debug("Publishing custom test message: {}", msg);
            kafkaProducerService.sendMessage("submission-topic", msg);
        } catch (Exception e) {
            log.error("Failed to send custom test KafkaTask", e);
        }
    }
}

