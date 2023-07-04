package nl.ing.lovebird.kafka.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.Markers;
import nl.ing.lovebird.logging.MDCContextCreator;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.slf4j.Marker;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.listener.BatchErrorHandler;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.support.LoggingProducerListener;
import org.springframework.kafka.support.ProducerListener;

import javax.validation.ConstraintViolationException;
import java.util.UUID;

@ConditionalOnClass(Producer.class)
@AutoConfiguration(before = KafkaAutoConfiguration.class)
@Slf4j
public class YoltKafkaLoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ProducerListener.class)
    public ProducerListener<Object, Object> kafkaProducerListener() {
        LoggingProducerListener<Object, Object> loggingListenerProducer = new LoggingProducerListener<>();
        loggingListenerProducer.setIncludeContents(false);
        return loggingListenerProducer;
    }

    @Bean
    @ConditionalOnMissingBean(ErrorHandler.class)
    public static ErrorHandler kafkaListenerErrorHandler() {
        return new KafkaErrorHandler();
    }

    @Bean
    @ConditionalOnMissingBean(BatchErrorHandler.class)
    public static BatchErrorHandler kafkaBatchListenerErrorHandler() {
        return new KafkaBatchErrorHandler();
    }

    @Slf4j
    private static class KafkaBatchErrorHandler implements BatchErrorHandler {

        @Override
        public void handle(Exception thrownException, ConsumerRecords<?, ?> data) {
            final String message = extractMessage(thrownException);
            log.error("Failed with exception: '{}' while processing message batch", message, thrownException);
        }
    }

    @Slf4j
    private static class KafkaErrorHandler implements ErrorHandler {
        public void handle(Exception thrownException, ConsumerRecord<?, ?> data) {
            final ConsumerRecord<String, byte[]> record = (ConsumerRecord<String, byte[]>) data;
            final String message = extractMessage(thrownException);
            final Marker marker = extractMarker(record);
            final String topic = extractTopic(record);
            log.error(marker, "Failed with exception: '{}' while processing message from topic '{}'", message, topic, thrownException);
        }

        private static Marker extractMarker(ConsumerRecord<String, byte[]> record) {
            // Add user_id to the log entry. This works only for new Kafka key format that contains only userId.
            try {
                return Markers.append(MDCContextCreator.USER_ID_HEADER_NAME, UUID.fromString(record.key()));
            } catch (Exception e) { // NOSONAR
                return Markers.append(MDCContextCreator.USER_ID_HEADER_NAME, "");
            }
        }

        private static String extractTopic(ConsumerRecord<String, byte[]> record) {
            return record != null ? record.topic() : null;
        }
    }

    private static String extractMessage(Exception thrownException) {
        final Throwable cause = thrownException.getCause();
        if (cause == null) {
            return thrownException.getMessage();
        }

        if (cause instanceof ConstraintViolationException) {
            ConstraintViolationException constraintViolationException = (ConstraintViolationException) cause;
            return constraintViolationException.getConstraintViolations().toString();
        }

        return cause.getMessage();
    }
}
