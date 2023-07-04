package com.yolt.compliance.gdpr.client;

import lombok.Getter;
import lombok.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.lang.NonNull;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

import static com.yolt.compliance.gdpr.client.messaging.producer.GDPRDataReplyProducer.KAFKA_HEADER_X_GDPR_REQUEST_ID;
import static com.yolt.compliance.gdpr.client.messaging.producer.GDPRDataReplyProducer.KAFKA_HEADER_X_GDPR_SERVICE_ID;
import static com.yolt.compliance.gdpr.client.messaging.producer.GDPRDataReplyProducer.KAFKA_HEADER_X_USER_ID;

@Getter
@Component
public class TestGDPRDataReplyConsumer {
    private final List<Received> received = new ArrayList<>();

    @KafkaListener(topics = "${yolt.kafka.topics.gdpr-replies-v2.topic-name}", concurrency = "${yolt.kafka.topics.gdpr-replies-v2.listener-concurrency}")
    public void onReceive(
            @Valid @Payload final byte[] data,
            @NonNull @Header(KAFKA_HEADER_X_USER_ID) final String userId,
            @NonNull @Header(KAFKA_HEADER_X_GDPR_REQUEST_ID) final String requestId,
            @NonNull @Header(KAFKA_HEADER_X_GDPR_SERVICE_ID) final String serviceId
    ) {
        received.add(new Received(data, userId, requestId, serviceId));
    }

    public void reset() {
        received.clear();
    }

    @Value
    public static class Received {
        byte[] data;
        String userId;
        String requestId;
        String serviceId;
    }

}
