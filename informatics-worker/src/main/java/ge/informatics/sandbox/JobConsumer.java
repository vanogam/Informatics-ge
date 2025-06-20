package ge.informatics.sandbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ge.informatics.sandbox.fileservice.FileService;
import ge.informatics.sandbox.model.Stage;
import ge.informatics.sandbox.model.TestResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import ge.informatics.sandbox.model.Task;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class JobConsumer {
    private static final Logger log = LogManager.getLogger(JobConsumer.class);
    private final KafkaConsumer<String, String> consumer;
    private final Sandbox sandbox;
    private final FileService fileService;

    public JobConsumer(String bootstrapServers, String groupId, String id) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.consumer = new KafkaConsumer<>(props);
        this.sandbox = new Sandbox(id);
        this.fileService = FileService.getInstance(Config.get("fileservice.type"));
    }

    public void listenToSubmissionTopic() {
        String topic = "submission-topic";
        consumer.subscribe(Collections.singletonList(topic));

        log.info("Listening to topic: " + topic);

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        processMessage(record.value());
                        consumer.commitSync();
                    } catch (IOException e) {
                        log.error("Failed to process message {}", record.value(), e);
                        throw new RuntimeException(e);
                    }
                }
            }
        } finally {
            consumer.close();
        }
    }

    private void processMessage(String message) throws IOException {
        log.info("Received message: " + message);

        ObjectMapper objectMapper = new ObjectMapper();
        Task task = objectMapper.readValue(message, Task.class);
        if (task.stage() == Stage.COMPILATION) {
            Files.createDirectories(Path.of("/sandbox/tmp/"));
            fileService.downloadFile(Config.get("fileStorageDirectory.url") + "/" + task.contestId() + "/" + task.code(), task.submissionId(),
                                     "/sandbox/tmp/" + task.submissionId(),
                    sandbox,
                    true
                    );
            sandbox.compile(task, new File("/sandbox/tmp/" + task.submissionId()));
        } else if (task.stage() == Stage.TESTING) {
            TestResult result = sandbox.execute(task);
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            Config.loadCustomConfig(args[0]);
        }

        JobConsumer consumer = new JobConsumer("localhost:9092", "contest", "1");
        consumer.listenToSubmissionTopic();
    }
}