package ge.informatics.sandbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import ge.informatics.sandbox.dao.SubmissionTestResultDao;
import ge.informatics.sandbox.kafka.CallbackProducer;
import ge.informatics.sandbox.model.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static ge.informatics.sandbox.Utils.compressFile;

public class JobConsumer {
    private static final Logger log = LoggerFactory.getLogger(JobConsumer.class);
    private final KafkaConsumer<String, String> consumer;
    private final Sandbox sandbox;
    private final HeartbeatSender heartbeatSender;
    public boolean running = true;

    public JobConsumer(String bootstrapServers, String groupId, String id, String serverUrl) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumer = new KafkaConsumer<>(props);
        sandbox = new Sandbox(id);
        
        // Initialize heartbeat sender if server URL is provided
        if (serverUrl != null && !serverUrl.isEmpty()) {
            String workerUsername = System.getenv("WORKER_USERNAME");
            String workerPassword = System.getenv("WORKER_PASSWORD");
            
            if (workerUsername != null && workerPassword != null && !workerUsername.isEmpty() && !workerPassword.isEmpty()) {
                heartbeatSender = new HeartbeatSender(serverUrl, id, workerUsername, workerPassword);
                heartbeatSender.start();
                log.info("Heartbeat sender started for worker: {}", id);
            } else {
                heartbeatSender = null;
                log.warn("Worker credentials not provided (WORKER_USERNAME and WORKER_PASSWORD), heartbeat sender disabled");
            }
        } else {
            heartbeatSender = null;
            log.warn("Server URL not provided, heartbeat sender disabled");
        }
    }

    public void listenToSubmissionTopic() {
        String topic = "submission-topic";
        consumer.subscribe(Collections.singletonList(topic));

        log.info("Listening to topic: {}", topic);

        try {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                if (records.count() > 0) {
                    log.info("Got records: {}", records.count());
                } else {
                    continue;
                }
                for (ConsumerRecord<String, String> record : records) {
                    Task task;
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        task = objectMapper.readValue(record.value(), Task.class);
                    } catch (Exception e) {
                        log.error("Error reading task json from message {}", record.value(), e);
                        sendCallback(new TestResult.Builder()
                                .withMessageType(CallbackType.SYSTEM_ERROR)
                                .withMessage("Invalid message format")
                                .build());
                        continue;
                    }
                    try {
                        // Set working status when starting to process
                        if (heartbeatSender != null) {
                            heartbeatSender.setWorking(true);
                        }
                        processMessage(task);
                        // Increment jobs processed counter after successful processing
                        if (heartbeatSender != null) {
                            heartbeatSender.incrementJobsProcessed();
                        }
                    } catch (Exception e) {
                        log.error("Failed to process message {}", record.value(), e);
                        sendCallback(new TestResult.Builder()
                                .withSubmissionId(Long.parseLong(task.submissionId()))
                                .withMessageType(CallbackType.SYSTEM_ERROR)
                                .withTestcaseKey(task.testId())
                                .build()
                        );
                    } finally {
                        // Clear working status when done processing
                        if (heartbeatSender != null) {
                            heartbeatSender.setWorking(false);
                        }
                    }
                }
                consumer.commitSync();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    return;
                }
            }
        } finally {
            try {
                if (heartbeatSender != null) {
                    heartbeatSender.stop();
                }
                sandbox.close();
            } catch (Exception e) {
                log.error("Error closing sandbox", e);
            }
            consumer.close();
        }
    }

    private void processMessage(Task task) throws IOException, InterruptedException {
        if (task.stage() == Stage.COMPILATION) {
            sendCallback(new TestResult.Builder()
                    .withSubmissionId(Long.parseLong(task.submissionId()))
                    .withMessageType(CallbackType.COMPILATION_STARTED)
                    .build());
            CompilationResult result = sandbox.compile(task, new File(Config.get("fileStorageDirectory.url") + "/" + task.taskId() + "/submissions/" + task.submissionName()));
            sendCallback(new TestResult.Builder()
                    .withSubmissionId(Long.parseLong(task.submissionId()))
                    .withMessageType(result.isSuccess() ? CallbackType.COMPILATION_COMPLETED : CallbackType.COMPILATION_FAILED)
                    .withMessage(result.getErrorMessage())
                    .build());
        } else if (task.stage() == Stage.TESTING) {
            String testsPath = Config.get("fileStorageDirectory.url") + "/" + task.taskId();
            if (!sandbox.fileExists("/sandbox/tasks/" + task.taskId())) {
                sandbox.uploadTar(compressFile(new File(testsPath), task.taskId(), "^("+ task.taskId() +"|tests)$"), "/sandbox/tasks/");
            } else {
                String lastUpdateText = sandbox.readFile("/sandbox/tasks/" + task.taskId() + "/lastUpdate.txt");
                long lastUpdate = 0;
                if (lastUpdateText != null && !lastUpdateText.isEmpty()) {
                    lastUpdate = Long.parseLong(lastUpdateText);
                }
                Path lastUpdatePath = new File(testsPath + "/lastUpdate").toPath();
                long currentUpdate = 0;
                if (Files.exists(lastUpdatePath)) {
                    currentUpdate = Long.parseLong(Files.readString(lastUpdatePath));
                }
                if (currentUpdate > lastUpdate) {
                    log.info("Task {} has been updated, re-uploading files", task.taskId());
                    sandbox.uploadTar(compressFile(new File(testsPath), task.taskId(), "^("+ task.taskId() +"|tests)$"), "/sandbox/tasks/");
                } else {
                    log.info("Task {} has not been updated, skipping upload", task.taskId());
                }
            }
            TestResult result = sandbox.execute(task);
            String outcome = sandbox.retrieveOutcome();
            SubmissionTestResultDao.saveTestResult(task.submissionId(),
                    task.testId(),
                    result.getScore(),
                    result.getStatus(),
                    result.getMessage(),
                    (int)result.getTimeMillis(),
                    (int)result.getMemoryKB(),
                    outcome);
            sendCallback(result);
        }
    }

    private void sendCallback(TestResult result) {
        CallbackProducer producer = new CallbackProducer(System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
        producer.sendTestResult(result);
        producer.close();
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            Config.loadCustomConfig(args[0]);
        }
        String serverUrl = System.getenv("SERVER_URL");
        if (serverUrl == null || serverUrl.isEmpty()) {
            // Default to http://main:8080 if not specified (for docker-compose)
            serverUrl = "http://main:8080";
        }
        JobConsumer consumer = new JobConsumer("kafka:9092", "worker", System.getenv("APP_ID"), serverUrl);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown signal received. Terminating...");
            consumer.running = false;
            try {
                consumer.sandbox.close();
                consumer.consumer.close();

            } catch (Exception ignored) {
            }
        }));
        consumer.listenToSubmissionTopic();
    }
}