package com.yolt.sample.kafka;

import nl.ing.lovebird.kafka.test.EnableExternalKafkaTestCluster;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@ActiveProfiles("test")
@SpringBootTest
@EnableExternalKafkaTestCluster
class ExampleIntegrationTest {

    final UUID userId = UUID.randomUUID();
    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    PongTestConsumer pongTestConsumer;

    @AfterEach
    void cleanup(){
        pongTestConsumer.removeAllForUser(userId);
    }

    @Test
    void pingPong() throws Exception {
        MessageDto ping = new MessageDto(userId, "ping");

        Message<MessageDto> message = MessageBuilder
                .withPayload(ping)
                .setHeader(KafkaHeaders.TOPIC, "ping")
                .setHeader(KafkaHeaders.MESSAGE_KEY, userId.toString())
                .build();

        kafkaTemplate.send(message).get();

        await()
                .pollInterval(1, TimeUnit.SECONDS)
                .timeout(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(pongTestConsumer.getAllForUserId(userId))
                        .isNotEmpty()
                        .allMatch(messageDto -> messageDto.getPayload().getUserId().equals(userId))
                        .allMatch(messageDto -> messageDto.getPayload().getContents().equals("pong")));
    }
}
