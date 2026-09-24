package ge.freeuni.informatics.judgeintegration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String message) {
        sendMessage(topic, null, message);
    }

    /**
     * Publishes with a partition key.
     *
     * <p>Unkeyed records go through the sticky partitioner, which fills one partition per batch -
     * so a submission's tests, published in a tight loop, all land on the same partition and are
     * served to a single worker. A key that varies per test hashes them across partitions instead,
     * which is what lets workers share one submission.
     */
    public void sendMessage(String topic, String key, String message) {
        // send() is fire-and-forget by default: without inspecting the returned future, a broker
        // being unreachable or the topic not existing yet fails the publish with nothing logged
        // anywhere, leaving a submission stuck IN_QUEUE with no trace of why.
        kafkaTemplate.send(topic, key, message).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish message to topic {} (key={})", topic, key, ex);
            } else {
                log.debug("Published message to topic {} partition {} offset {} (key={})",
                        topic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset(), key);
            }
        });
    }
}