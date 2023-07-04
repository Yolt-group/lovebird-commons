package nl.ing.lovebird.kafka.autoconfigure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.autoconfigure.ClientTokenConverterAutoConfiguration;
import nl.ing.lovebird.clienttokens.autoconfigure.ClientTokenParserAutoConfiguration;
import nl.ing.lovebird.clienttokens.autoconfigure.test.ClientTokenTestParserAutoConfiguration;
import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import nl.ing.lovebird.clienttokens.test.TestClientTokens;
import nl.ing.lovebird.kafka.test.EnableExternalKafkaTestCluster;
import nl.ing.lovebird.kafka.test.KafkaExternalTestClusterContextCustomizerFactory;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.annotation.UserConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextCustomizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class YoltKafkaIntegrationTest {

    public static final String TEST_TOPIC = "kafka-test-topic";

    private static final UUID CLIENT_GROUP_ID = UUID.randomUUID();
    private static final UUID CLIENT_ID = UUID.randomUUID();

    final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withPropertyValues(
                    "spring.kafka.consumer.group-id=test-application",
                    "spring.kafka.consumer.auto-offset-reset=earliest"
            )
            .withConfiguration(AutoConfigurations.of(
                    JacksonAutoConfiguration.class,
                    KafkaAutoConfiguration.class,
                    KafkaHealthIndicatorAutoConfiguration.class,
                    YoltKafkaAutoConfiguration.class,
                    /*
                     * Note: Kafka and client tokens are *not* strongly coupled.
                     * The converters registered by ClientTokenConverterAutoConfiguration
                     * handle the conversion. But we have to test the whole system somewhere.
                     */
                    ClientTokenParserAutoConfiguration.class,
                    ClientTokenConverterAutoConfiguration.class,
                    ClientTokenTestParserAutoConfiguration.class
            ))
            .withInitializer(applicationContext -> {
                @EnableExternalKafkaTestCluster
                class ExampleTest {

                }
                ContextCustomizer contextCustomizer = new KafkaExternalTestClusterContextCustomizerFactory()
                        .createContextCustomizer(ExampleTest.class, Collections.emptyList());
                contextCustomizer.customizeContext(applicationContext, null);
            })
            .withConfiguration(UserConfigurations.of(TestKafkaConsumer.class));

    @SneakyThrows
    private static TestPayload produceTestMessage(KafkaTemplate<String, TestPayload> kafkaTemplate, UUID userId, Object clientToken) {
        TestPayload testPayload = new TestPayload(42, "someString", new Date());

        Message<TestPayload> message = MessageBuilder
                .withPayload(testPayload)
                .setHeader(KafkaHeaders.TOPIC, TEST_TOPIC)
                .setHeader(KafkaHeaders.MESSAGE_KEY, userId.toString())
                // Note: Should always be a string, using object to test.
                .setHeader(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME, clientToken)
                .build();

        kafkaTemplate.send(message).get();

        return testPayload;
    }

    private static void assertSingleTestMessageConsumed(TestKafkaConsumer consumer, UUID userId, ClientToken clientToken, TestPayload testPayload) {
        await().atMost(10, SECONDS).untilAsserted(() -> {
            List<TestKafkaConsumer.Consumed> consumedList = consumer.getConsumed();
            assertThat(consumedList).hasSize(1);
            TestKafkaConsumer.Consumed consumed = consumedList.get(0);
            assertThat(consumed.getKey()).isEqualTo(userId.toString());
            assertThat(consumed.getClientToken()).isEqualTo(clientToken);
            assertThat(consumed.getPayload()).isEqualTo(testPayload);
        });
    }

    @Test
    void healthShouldIndicateUp() {
        runner.run(context -> {
            HealthIndicator healthIndicator = context.getBean("kafkaHealthIndicator", HealthIndicator.class);
            assertThat(healthIndicator.health().getStatus()).isEqualTo(Status.UP);
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldHandleClientTokenHeadersAsString() {
        runner.run(context -> {
            UUID userId = UUID.randomUUID();
            TestClientTokens testClientTokens = context.getBean(TestClientTokens.class);
            ClientToken clientToken = testClientTokens.createClientToken(CLIENT_GROUP_ID, CLIENT_ID);

            KafkaTemplate<String, TestPayload> kafkaTemplate = context.getBean(KafkaTemplate.class);
            TestPayload testPayload = produceTestMessage(kafkaTemplate, userId, clientToken.getSerialized());

            TestKafkaConsumer consumer = context.getBean(TestKafkaConsumer.class);
            assertSingleTestMessageConsumed(consumer, userId, clientToken, testPayload);
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldNotConsumeMessageWithInvalidClientToken() {
        runner.run(context -> {
            UUID userId = UUID.randomUUID();
            TestClientTokens testClientTokens = context.getBean(TestClientTokens.class);
            ClientToken clientToken = testClientTokens.createClientToken(CLIENT_GROUP_ID, CLIENT_ID);
            ClientToken invalidClientToken = new ClientToken("invalid", new JwtClaims());

            KafkaTemplate<String, TestPayload> kafkaTemplate = context.getBean(KafkaTemplate.class);
            produceTestMessage(kafkaTemplate, userId, invalidClientToken.getSerialized());
            TestPayload testPayload = produceTestMessage(kafkaTemplate, userId, clientToken.getSerialized());
            // only 1 of 2 messages should be consumed
            TestKafkaConsumer consumer = context.getBean(TestKafkaConsumer.class);
            assertSingleTestMessageConsumed(consumer, userId, clientToken, testPayload);
        });
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldIgnoreClientTokenHeaderAsObjects() {
        runner.run(context -> {
            UUID userId = UUID.randomUUID();

            TestClientTokens testClientTokens = context.getBean(TestClientTokens.class);
            ClientToken clientToken = testClientTokens.createClientToken(CLIENT_GROUP_ID, CLIENT_ID);

            KafkaTemplate<String, TestPayload> kafkaTemplate = context.getBean(KafkaTemplate.class);
            TestPayload testPayload = produceTestMessage(kafkaTemplate, userId, clientToken);

            TestKafkaConsumer consumer = context.getBean(TestKafkaConsumer.class);
            assertSingleTestMessageConsumed(consumer, userId, null, testPayload);
        });
    }

    @Component
    public static class TestKafkaConsumer {

        private final List<Consumed> consumed = new ArrayList<>();

        @KafkaListener(topics = TEST_TOPIC, groupId = "testKafkaConsumer-groupId")
        public void consume(
                @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) final String key,
                @Header(value = ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME, required = false) final ClientToken clientToken,
                @Payload final TestPayload payload
        ) {
            consumed.add(new Consumed(key, clientToken, payload));
        }

        public List<Consumed> getConsumed() {
            return consumed;
        }

        @Data
        @AllArgsConstructor
        public static class Consumed {
            private final String key;
            private final ClientToken clientToken;
            private final TestPayload payload;
        }
    }

    @Data
    public static class TestPayload {

        private final int someInt;
        private final String someString;
        private final Date someDate;
    }
}
