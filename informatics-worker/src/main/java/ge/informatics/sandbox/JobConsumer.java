package ge.informatics.sandbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import ge.informatics.sandbox.model.Task;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class JobConsumer {
    private static final Logger log = LogManager.getLogger(JobConsumer.class);
    private final KafkaConsumer<String, String> consumer;
    private final Sandbox sandbox;

    public JobConsumer(String bootstrapServers, String groupId, String id) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        this.consumer = new KafkaConsumer<>(props);
        this.sandbox = new Sandbox(id);
    }

    public void listenToContestTopic() {
        String topic = "contest";
        consumer.subscribe(Collections.singletonList(topic));

        log.info("Listening to topic: " + topic);

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        processMessage(record.value());
                    } catch (JsonProcessingException e) {
                        log.error("Failed to process message {}", record.value(), e);
                        throw new RuntimeException(e);
                    }
                }
            }
        } finally {
            consumer.close();
        }
    }

    private void processMessage(String message) throws JsonProcessingException {
        // Assuming the message is in JSON format: {"taskCode": "T1", "testId": "01", "submissionId": "123"}
        log.info("Received message: " + message);

        ObjectMapper objectMapper = new ObjectMapper();
        Task task = objectMapper.readValue(message, Task.class);



        // Add further processing logic here
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            Config.loadCustomConfig(args[0]);
        }

        JobConsumer consumer = new JobConsumer("localhost:9092", "contest", "1");
        consumer.listenToContestTopic();
    }
}