package ge.freeuni.informatics.judgeintegration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
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
        kafkaTemplate.send(topic, key, message);
    }
}