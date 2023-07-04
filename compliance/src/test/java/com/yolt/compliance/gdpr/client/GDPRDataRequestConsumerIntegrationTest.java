package com.yolt.compliance.gdpr.client;

import com.yolt.compliance.gdpr.client.messaging.consumer.protocol.GDPRDataRequest;
import com.yolt.compliance.gdpr.client.spi.GDPRServiceDataProvider.FileMetaAndBytes;
import com.yolt.compliance.gdpr.client.spi.GDPRServiceDataProvider.FileMetadata;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Optional;

import static com.yolt.compliance.gdpr.client.TestConstants.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

class GDPRDataRequestConsumerIntegrationTest extends IntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> producer;

    @Test
    void shouldGetFileAndSendItUpstream() {

        // Expected

        byte[] data = {0x01, 0x02, 0x03, 0x4};
        when(dataProvider.getDataFileAsBytes(USER_ID_1)).thenReturn(
                Optional.of(new FileMetaAndBytes(new FileMetadata("file-name", "file-format"), data)));
        when(dataProvider.getServiceId()).thenReturn(SERVICE_ID_1);

        final GDPRDataRequest dataRequest = GDPRDataRequest.builder()
                .userId(USER_ID_1)
                .requestId(REQUEST_ID_1)
                .build();

        producer.send(new ProducerRecord<>("gdprRequests", USER_ID_1.toString(), dataRequest));

        await().atMost(10, SECONDS).untilAsserted(() -> {
            List<TestGDPRDataReplyConsumer.Received> receiveds = testGDPRDataReplyConsumer.getReceived();
            assertThat(receiveds).isNotEmpty();
            TestGDPRDataReplyConsumer.Received received = receiveds.get(0);
            assertThat(received.getData()).isEqualTo(data);
            assertThat(received.getServiceId()).isEqualTo(SERVICE_ID_1);
            assertThat(received.getRequestId()).isEqualTo(REQUEST_ID_1.toString());
            assertThat(received.getUserId()).isEqualTo(USER_ID_1.toString());
        });

        verify(dataProvider, timeout(1000)).getDataFileAsBytes(USER_ID_1);
    }
}
