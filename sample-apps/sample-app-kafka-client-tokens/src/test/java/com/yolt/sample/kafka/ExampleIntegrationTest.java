package com.yolt.sample.kafka;

import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import nl.ing.lovebird.clienttokens.test.TestClientTokens;
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
    final UUID clientGroupId = UUID.randomUUID();
    final UUID clientId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    PongTestConsumer pongTestConsumer;

    @Autowired
    TestClientTokens testClientTokens;

    @AfterEach
    void cleanup() {
        pongTestConsumer.removeAllForUser(userId);
    }

    @Test
    void pingPong() throws Exception {
        ClientUserToken clientUserToken = testClientTokens.createClientUserToken(clientGroupId, clientId, userId);

        MessageDto ping = new MessageDto("ping");

        Message<MessageDto> message = MessageBuilder
                .withPayload(ping)
                .setHeader(KafkaHeaders.TOPIC, "ping")
                .setHeader(KafkaHeaders.MESSAGE_KEY, userId.toString())
                .setHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME, clientUserToken.getSerialized())
                .build();

        kafkaTemplate.send(message).get();

        await()
                .pollInterval(1, TimeUnit.SECONDS)
                .timeout(10, TimeUnit.SECONDS)
                .untilAsserted(() -> assertThat(pongTestConsumer.getAllForUserId(userId))
                        .isNotEmpty()
                        .allMatch(event -> event.getClientUserToken().equals(clientUserToken))
                        .allMatch(event -> event.getPayload().getContents().equals("pong")));
    }
}
