package ge.informatics.sandbox.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import ge.informatics.sandbox.model.TestResult;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class CallbackProducer {
    private static final Logger log = LoggerFactory.getLogger(CallbackProducer.class);
    private final KafkaProducer<String, String> producer;
    private final ObjectMapper objectMapper;

    public CallbackProducer(String bootstrapServer) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        this.producer = new KafkaProducer<>(props);
        this.objectMapper = new ObjectMapper();
    }

    public void sendTestResult(TestResult testResult) {
        try {
            String message = objectMapper.writeValueAsString(testResult);
            ProducerRecord<String, String> record = new ProducerRecord<>("submission-callback", message);
            producer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Failed to send TestResult for submission {}", testResult.getSubmissionId(), exception);
                } else {
                    log.info("TestResult sent successfully for submission {} to topic {}", testResult.getSubmissionId(), metadata.topic());
                }
            });
        } catch (Exception e) {
            log.error("Error while serializing TestResult for submission {}", testResult.getSubmissionId(), e);
        }
    }

    public void close() {
        producer.close();
    }
}