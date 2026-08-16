package ge.informatics.sandbox;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ge.informatics.sandbox.kafka.CallbackProducer;
import ge.informatics.sandbox.model.*;
import org.apache.kafka.clients.consumer.CommitFailedException;
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
    /**
     * Task subdirectories the sandbox needs. Graders are compiled into the submission and the
     * manager judges it, so both have to be present before compilation, not just before testing.
     */
    private static final String TASK_DIR_FILTER = "|tests|custom-tests|graders|manager|checker)$";
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
        // One test per poll. The default of 500 lets a single batch run for many minutes, which
        // blows past max.poll.interval.ms: Kafka then evicts this consumer, hands its partitions
        // to another worker - which replays the same submission - and fails the commit here.
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        // Generous headroom for one slow test, so eviction needs a genuine hang rather than a
        // merely large task.
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 900000);
        // Offsets are committed explicitly once a job is done; auto-commit could acknowledge a
        // record that was never processed.
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

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
                        // Tolerate unknown fields so a core deployed ahead of its workers does
                        // not fill the topic with messages no worker can read.
                        ObjectMapper objectMapper = new ObjectMapper()
                                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
                try {
                    consumer.commitSync();
                } catch (CommitFailedException e) {
                    // Evicted from the group mid-batch. The work is already done; another
                    // consumer may redo it, but killing this worker helps nobody.
                    log.error("Offset commit failed, continuing. This worker was likely evicted "
                            + "from the consumer group because a job outran max.poll.interval.ms", e);
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
            // Graders are linked into the submission, so the task files must be in place first.
            syncTaskFiles(task);
            CompilationResult result = sandbox.compile(task, new File(Config.get("fileStorageDirectory.url") + "/" + task.taskId() + "/submissions/" + task.submissionName()));
            sendCallback(new TestResult.Builder()
                    .withSubmissionId(Long.parseLong(task.submissionId()))
                    .withMessageType(result.isSuccess() ? CallbackType.COMPILATION_COMPLETED : CallbackType.COMPILATION_FAILED)
                    .withMessage(result.getErrorMessage())
                    .build());
        } else if (task.stage() == Stage.TESTING) {
            syncTaskFiles(task);
            TestResult result = sandbox.execute(task);
            sendCallback(result);
        }
    }

    /**
     * Copies the task's tests, graders and manager into the sandbox, skipping the upload when
     * the sandbox already holds the current version.
     */
    private void syncTaskFiles(Task task) throws IOException, InterruptedException {
        String taskPath = Config.get("fileStorageDirectory.url") + "/" + task.taskId();
        String filter = "^(" + task.taskId() + TASK_DIR_FILTER;

        if (!sandbox.fileExists("/sandbox/tasks/" + task.taskId())) {
            sandbox.uploadTar(compressFile(new File(taskPath), task.taskId(), filter), "/sandbox/tasks/");
            return;
        }
        long lastUpdate = parseTimestamp(sandbox.readFile("/sandbox/tasks/" + task.taskId() + "/lastUpdate"));
        Path lastUpdatePath = new File(taskPath + "/lastUpdate").toPath();
        long currentUpdate = 0;
        if (Files.exists(lastUpdatePath)) {
            currentUpdate = parseTimestamp(Files.readString(lastUpdatePath));
        }
        if (currentUpdate > lastUpdate) {
            log.info("Task {} has been updated, re-uploading files", task.taskId());
            // Replace rather than merge, so deleted graders and tests actually go away.
            sandbox.clearTaskDirectory(task.taskId());
            sandbox.uploadTar(compressFile(new File(taskPath), task.taskId(), filter), "/sandbox/tasks/");
        } else {
            log.info("Task {} has not been updated, skipping upload", task.taskId());
        }
    }

    private long parseTimestamp(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            log.warn("Unreadable lastUpdate marker '{}', treating the task as stale", text);
            return 0;
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
            serverUrl = "http://main:8080";
        }
        JobConsumer consumer = new JobConsumer("kafka:9092", "worker", System.getenv("APP_ID"), serverUrl);

        // Runs on any JVM exit, including an unhandled exception - so it must not claim a signal
        // was received, and must not double-close what listenToSubmissionTopic already closed.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("JVM shutting down, stopping worker...");
            consumer.running = false;
        }));

        try {
            consumer.listenToSubmissionTopic();
        } catch (Exception e) {
            // Otherwise the worker dies with only the shutdown hook's message in the log, which
            // reads as a clean stop rather than the crash it actually is.
            log.error("Worker stopped because the consumer loop failed", e);
            throw e;
        }
    }
}