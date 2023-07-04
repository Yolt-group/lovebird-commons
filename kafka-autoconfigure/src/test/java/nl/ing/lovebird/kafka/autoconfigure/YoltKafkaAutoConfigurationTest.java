package nl.ing.lovebird.kafka.autoconfigure;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.Serializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.actuate.autoconfigure.metrics.KafkaMetricsAutoConfiguration;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.MicrometerConsumerListener;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.BatchErrorHandler;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.RecordMessageConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.ing.lovebird.kafka.autoconfigure.TestCertificates.ISSUING_CA;
import static nl.ing.lovebird.kafka.autoconfigure.TestCertificates.POD_CERT;
import static nl.ing.lovebird.kafka.autoconfigure.TestCertificates.PRIVATE_KEY;
import static org.assertj.core.api.Assertions.assertThat;

class YoltKafkaAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    JacksonAutoConfiguration.class,
                    YoltKafkaAutoConfiguration.class,
                    YoltKafkaLoggingAutoConfiguration.class,
                    KafkaHealthIndicatorAutoConfiguration.class,
                    KafkaAutoConfiguration.class,
                    YoltKafkaProperties.class
            ));

    @Test
    @DisplayName("[SHOULD] create kafka-related beans [GIVEN] Producer bean is present on classpath")
    void shouldCreateBeansIfProducerIsOnClasspath() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ConsumerFactory.class);
            assertThat(context).hasSingleBean(RecordMessageConverter.class);
            assertThat(context).hasSingleBean(Serializer.class);
            assertThat(context).hasSingleBean(ProducerFactory.class);
            assertThat(context).hasSingleBean(ErrorHandler.class);
            assertThat(context).hasSingleBean(BatchErrorHandler.class);
            assertThat(context).hasSingleBean(ProducerListener.class);
            assertThat(context).hasSingleBean(HealthIndicator.class);
        });
    }

    @Test
    @DisplayName("[SHOULD] create KafkaProducerHealthIndicator bean [GIVEN] health topic is set")
    void testHealth() {
        contextRunner
                .withPropertyValues("lovebird.kafka.health.topic=testHealthTopic")
                .withPropertyValues("lovebird.kafka.health.timeout=0s")
                .withBean(MockProducer.class)
                .run(context -> assertThat(context).hasSingleBean(YoltKafkaAutoConfiguration.KafkaProducerHealthIndicator.class));
    }

    @Test
    @DisplayName("[SHOULD NOT] create kafka-related beans [GIVEN] Producer bean is absent on classpath")
    void shouldNotCreateBeansIfProducerIsNotOnClasspath() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(Producer.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RecordMessageConverter.class);
                    assertThat(context).doesNotHaveBean(Serializer.class);
                    assertThat(context).doesNotHaveBean(ErrorHandler.class);
                    assertThat(context).doesNotHaveBean(BatchErrorHandler.class);
                });
    }

    @Test
    void shouldAddMetricListenerToConsumerIfInContext() {
        contextRunner
                .withConfiguration(AutoConfigurations.of(KafkaMetricsAutoConfiguration.class))
                .withBean(SimpleMeterRegistry.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(ConsumerFactory.class);
                    List<?> listeners = context.getBean(DefaultKafkaConsumerFactory.class).getListeners();
                    assertThat(listeners).hasSize(1)
                            .hasAtLeastOneElementOfType(MicrometerConsumerListener.class);
                });
    }

    @Test
    void whenVaultInjectorIsUsedShouldWireCorrectBean(@TempDir Path tempDir) throws IOException {
        Files.write(tempDir.resolve("kafka_cert"), POD_CERT.getBytes(UTF_8));
        Files.write(tempDir.resolve("kafka_issuing_ca"), ISSUING_CA.getBytes(UTF_8));
        Files.write(tempDir.resolve("kafka_private_key"), PRIVATE_KEY.getBytes(UTF_8));

        contextRunner
                .withPropertyValues(
                        "yolt.vault.kafka.enabled=true",
                        "yolt.vault.secrets.directory=" + tempDir
                )
                .run(c -> assertThat(c)
                        .hasSingleBean(VaultKafkaKeystoreInitializer.class)
                        .hasSingleBean(ConsumerFactory.class)
                );
    }

    @Test
    void shouldWireKafkaPropertiesInConsumerFactory(@TempDir Path tempDir) throws IOException {
        Files.write(tempDir.resolve("kafka_cert"), POD_CERT.getBytes(UTF_8));
        Files.write(tempDir.resolve("kafka_issuing_ca"), ISSUING_CA.getBytes(UTF_8));
        Files.write(tempDir.resolve("kafka_private_key"), PRIVATE_KEY.getBytes(UTF_8));

        contextRunner
                .withPropertyValues(
                        "yolt.vault.kafka.enabled=true",
                        "yolt.vault.secrets.directory=" + tempDir
                )
                .run(c -> {
                    DefaultKafkaConsumerFactory<?, ?> bean = c.getBean(DefaultKafkaConsumerFactory.class);

                    assertThat(bean.getConfigurationProperties())
                            .hasEntrySatisfying("ssl.truststore.location", s -> assertThat(s).isEqualTo(tempDir + "/kafka_trust_store.jks"));
                    assertThat(bean.getConfigurationProperties())
                            .hasEntrySatisfying("ssl.keystore.location", s -> assertThat(s).isEqualTo(tempDir + "/kafka_key_store.jks"));
                });
    }

}
