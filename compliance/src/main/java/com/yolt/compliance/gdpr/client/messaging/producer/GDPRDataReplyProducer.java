package com.yolt.compliance.gdpr.client.messaging.producer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import static java.util.Optional.ofNullable;

@Slf4j
@Component
public class GDPRDataReplyProducer {

    public static final String KAFKA_HEADER_X_USER_ID = "X-USER-ID";
    public static final String KAFKA_HEADER_X_GDPR_SERVICE_ID = "X-GDPR-SERVICE-ID";
    public static final String KAFKA_HEADER_X_GDPR_REQUEST_ID = "X-GDPR-REQUEST-ID";

    private final KafkaTemplate<String, byte[]> producer;
    private final String replyTopic;

    public GDPRDataReplyProducer(
            @Value("${yolt.kafka.topics.gdpr-replies-v2.topic-name}") final String replyTopic,
            KafkaTemplate<String, byte[]> kafkaTemplate
    ) {
        this.replyTopic = replyTopic;
        this.producer = kafkaTemplate;
    }

    /**
     * Send a serialized {@see GDPRDataReply} and associated data to the default topic assigned to the {@see producer}
     *
     * @param meta the GDPR metadata
     * @param data the raw GDPR data
     */
    public void reply(@NonNull final GDPRDataReply meta, @NonNull byte[] data) {

        final Message<byte[]> message = MessageBuilder
                .withPayload(data)
                .setHeader(KafkaHeaders.TOPIC, replyTopic)
                .setHeader(KafkaHeaders.MESSAGE_KEY, meta.getUserId().toString())
                .setHeader(KAFKA_HEADER_X_USER_ID, meta.getUserId().toString())
                .setHeader(KAFKA_HEADER_X_GDPR_SERVICE_ID, meta.getServiceId())
                .setHeader(KAFKA_HEADER_X_GDPR_REQUEST_ID, meta.getRequestId().toString())
                .build();

        producer.send(message).completable().handle((result, throwable) -> {
            ofNullable(throwable).ifPresent(t -> {
                log.error("Failed to publish `message`", t);
            });
            return null;
        });
    }
}
