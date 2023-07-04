package com.yolt.compliance.gdpr.client;

import com.yolt.compliance.gdpr.client.messaging.producer.GDPRDataReplyProducer;
import com.yolt.compliance.gdpr.client.spi.GDPRServiceDataProvider;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.kafka.test.EnableExternalKafkaTestCluster;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@EnableExternalKafkaTestCluster
@DirtiesContext
abstract class IntegrationTest {

    @MockBean
    protected GDPRServiceDataProvider dataProvider;

    @Autowired
    protected GDPRDataReplyProducer dataReplyProducer;

    @Autowired
    protected TestGDPRDataReplyConsumer testGDPRDataReplyConsumer;


    @BeforeEach
    public void setup() {
        testGDPRDataReplyConsumer.reset();
    }

}
