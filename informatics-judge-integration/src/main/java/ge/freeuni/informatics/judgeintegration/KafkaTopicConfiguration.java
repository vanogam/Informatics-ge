package ge.freeuni.informatics.judgeintegration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares the judging topics with enough partitions to spread work across workers.
 *
 * <p>Every testcase is published as its own message, and the producer sets no key, so tests are
 * distributed round-robin. A consumer group only ever delivers a partition to one member though,
 * so a single-partition topic - the broker default for auto-created topics - pins an entire
 * submission to one worker while the rest idle.
 *
 * <p>KafkaAdmin applies these at startup and will raise the partition count of an existing topic,
 * so no manual alter is needed. Partition counts can only ever grow; lowering the setting later
 * has no effect.
 */
@Configuration
public class KafkaTopicConfiguration {

    /**
     * Should be at least the number of workers, otherwise the surplus workers get nothing.
     */
    @Value("${ge.freeuni.informatics.kafka.submissionTopicPartitions:16}")
    private int submissionTopicPartitions;

    /**
     * Callbacks are consumed by the core alone, which serialises per submission anyway, so this
     * only needs enough parallelism to keep up with the workers.
     */
    @Value("${ge.freeuni.informatics.kafka.callbackTopicPartitions:4}")
    private int callbackTopicPartitions;

    @Bean
    public NewTopic submissionTopic() {
        return TopicBuilder.name("submission-topic")
                .partitions(submissionTopicPartitions)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic submissionCallbackTopic() {
        return TopicBuilder.name("submission-callback")
                .partitions(callbackTopicPartitions)
                .replicas(1)
                .build();
    }
}
