package nl.ing.lovebird.kafka.autoconfigure;

import nl.ing.lovebird.kafka.autoconfigure.YoltKafkaAutoConfiguration.KafkaProducerHealthIndicator;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaProducerHealthIndicatorTest {
    private final static String HEALTH_TOPIC = "HEALTH_TOPIC";

    private KafkaProducerHealthIndicator healthIndicator;

    private RecordMetadata recordMetadata;

    @Mock
    private Producer<String, String> producer;

    @BeforeEach
    void setup() {
        healthIndicator = new KafkaProducerHealthIndicator("healthTopic", 1, producer, "test");

        // final classes
        final TopicPartition topicPartition = new TopicPartition(HEALTH_TOPIC, 0);
        recordMetadata = new RecordMetadata(topicPartition, 0, 0, 0, 0, 0);
    }

    @Test
    void testHappyFlow() {
        Future<RecordMetadata> future = CompletableFuture.completedFuture(recordMetadata);

        when(producer.send(any())).thenReturn(future);

        final Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
    }

    @Test
    void testTimeout() {
        Future<RecordMetadata> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException ignored){
            }
            return null;
        });

        when(producer.send(any())).thenReturn(future);

        final Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }

    @Test
    void testExceptionThrownWhenSendingMessage() {
        when(producer.send(any())).thenThrow(new RuntimeException("Exception"));

        final Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
    }
}
