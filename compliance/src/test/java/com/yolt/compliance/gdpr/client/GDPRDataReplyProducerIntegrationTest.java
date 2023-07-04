package com.yolt.compliance.gdpr.client;

import com.yolt.compliance.gdpr.client.messaging.producer.GDPRDataReply;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.yolt.compliance.gdpr.client.TestUtils.createDefaultGDPRDataReplyKeyBuilder;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


class GDPRDataReplyProducerIntegrationTest extends IntegrationTest {

    @Test
    void testSend() {
        final GDPRDataReply meta
                = createDefaultGDPRDataReplyKeyBuilder().build();
        dataReplyProducer.reply(meta, new byte[]{0x00});

        await().atMost(5, SECONDS).untilAsserted(() -> {
            final List<TestGDPRDataReplyConsumer.Received> message = testGDPRDataReplyConsumer.getReceived();
            assertThat(message).hasSize(1);

            TestGDPRDataReplyConsumer.Received received = message.get(0);
            assertThat(received.getData()).isEqualTo(new byte[]{0x00});
            assertThat(received.getUserId()).isEqualTo(meta.getUserId().toString());
            assertThat(received.getRequestId()).isEqualTo(meta.getRequestId().toString());
            assertThat(received.getServiceId()).isEqualTo(meta.getServiceId());

        });


    }
}
