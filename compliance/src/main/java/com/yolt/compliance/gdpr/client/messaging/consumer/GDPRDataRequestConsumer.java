package com.yolt.compliance.gdpr.client.messaging.consumer;

import com.yolt.compliance.gdpr.client.messaging.consumer.protocol.GDPRDataRequest;
import com.yolt.compliance.gdpr.client.messaging.producer.GDPRDataReply;
import com.yolt.compliance.gdpr.client.messaging.producer.GDPRDataReplyProducer;
import com.yolt.compliance.gdpr.client.spi.GDPRServiceDataProvider;
import com.yolt.compliance.gdpr.client.spi.GDPRServiceDataProvider.FileMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.validation.Valid;

@Slf4j
@Component
@RequiredArgsConstructor
public class GDPRDataRequestConsumer {

    private final GDPRServiceDataProvider dataProvider;
    private final GDPRDataReplyProducer replyProducer;

    @KafkaListener(topicPattern = "${yolt.kafka.topics.gdpr-requests.topic-name}", concurrency = "${yolt.kafka.topics.gdpr-requests.listener-concurrency}")
    public void onReceive(@Valid @Payload final GDPRDataRequest dataRequest) {
        try {
            tryOnReceive(dataRequest);
        } catch (Exception e) {
            log.error("A Kafka Listener exception was caught w/ message " + e.getMessage(), e);
            throw e;
        }
    }

    private void tryOnReceive(final GDPRDataRequest dataRequest) {

        log.info("Received GDPR data request {} for user {}", dataRequest.getRequestId(), dataRequest.getUserId());

        dataProvider.getDataFileAsBytes(dataRequest.getUserId())
                .ifPresent(metadataPair -> {

                    final FileMetadata metadata = metadataPair.getMetadata();
                    final byte[] data = metadataPair.getBytes();

                    final GDPRDataReply meta = GDPRDataReply.builder()
                            .userId(dataRequest.getUserId())
                            .requestId(dataRequest.getRequestId())
                            .serviceId(dataProvider.getServiceId())
                            .dataInfo(new GDPRDataReply.GDPRDataInfo(metadata.getName(), metadata.getFormat()))
                            .build();

                    replyProducer.reply(meta, data);
                });
    }
}
