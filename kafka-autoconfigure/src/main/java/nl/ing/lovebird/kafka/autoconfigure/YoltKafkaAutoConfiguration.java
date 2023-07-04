package nl.ing.lovebird.kafka.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.BytesSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.Bytes;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SimpleKafkaHeaderMapper;
import org.springframework.kafka.support.converter.BytesJsonMessageConverter;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.nio.file.Paths;
import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static nl.ing.lovebird.kafka.autoconfigure.VaultKafkaKeystoreInitializer.KAFKA_CERT_FILE_NAME;
import static nl.ing.lovebird.kafka.autoconfigure.VaultKafkaKeystoreInitializer.KAFKA_ISSUING_CA;
import static nl.ing.lovebird.kafka.autoconfigure.VaultKafkaKeystoreInitializer.KAFKA_PRIVATE_KEY;
import static nl.ing.lovebird.vault.Vault.requireFileProvidedByVault;

@ConditionalOnClass(Producer.class)
@AutoConfiguration(before = KafkaAutoConfiguration.class)
@EnableConfigurationProperties({YoltKafkaProperties.class, YoltVaultSecretsProperties.class})
@RequiredArgsConstructor
@Slf4j
public class YoltKafkaAutoConfiguration {

    private final KafkaProperties kafkaProperties;
    private final YoltKafkaProperties yoltKafkaProperties;
    private final YoltVaultSecretsProperties vaultProperties;

    @Bean
    @ConditionalOnProperty("lovebird.kafka.health.topic")
    @ConditionalOnBean(Producer.class)
    public KafkaProducerHealthIndicator kafkaProducerHealthIndicator(Producer<String, String> producer, @Value("${spring.application.name}") String applicationName) {
        String healthTopic = yoltKafkaProperties.getHealth().getTopic();
        Duration healthTimeout = yoltKafkaProperties.getHealth().getTimeout();
        return new KafkaProducerHealthIndicator(healthTopic, healthTimeout.toMillis(), producer, applicationName);
    }

    @Bean
    @ConditionalOnProperty(name = "yolt.vault.kafka.enabled", havingValue = "true")
    public VaultKafkaKeystoreInitializer vaultKafkaKeystoreInitializer() {
        String vaultSecretsDirectory = vaultProperties.getDirectory();
        requireFileProvidedByVault(Paths.get(vaultSecretsDirectory, KAFKA_CERT_FILE_NAME));
        requireFileProvidedByVault(Paths.get(vaultSecretsDirectory, KAFKA_ISSUING_CA));
        requireFileProvidedByVault(Paths.get(vaultSecretsDirectory, KAFKA_PRIVATE_KEY));

        log.info("Wiring with vault-agent kafka with directory: {}", vaultSecretsDirectory);
        VaultKafkaKeystoreInitializer initializer = new VaultKafkaKeystoreInitializer(vaultSecretsDirectory);
        initializer.initializeKeyStore();
        return initializer;
    }

    @Bean
    @ConditionalOnMissingBean(ConsumerFactory.class)
    @SuppressWarnings("squid:S1452") // Indicates to spring that this can create any consumer factory
    public ConsumerFactory<?, ?> kafkaConsumerFactory(Optional<VaultKafkaKeystoreInitializer> optionalVaultKafkaKeystoreInitializer,
                                                      ObjectProvider<DefaultKafkaConsumerFactoryCustomizer> customizers) {
        final Map<String, Object> consumerProperties = this.kafkaProperties.buildConsumerProperties();
        // Adds Kafka SSL configuration
        optionalVaultKafkaKeystoreInitializer.ifPresent(
                vk -> consumerProperties.putAll(vk.kafkaProperties())
        );

        DefaultKafkaConsumerFactory<String, byte[]> factory = new DefaultKafkaConsumerFactory<>(
                consumerProperties,
                new StringDeserializer(),
                new ByteArrayDeserializer()
        );

        customizers.orderedStream().forEach(customizer -> customizer.customize(factory));
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean(RecordMessageConverter.class)
    public RecordMessageConverter kafkaMessageConverter(ObjectMapper objectMapper) {
        BytesJsonMessageConverter messageConverter = new BytesJsonMessageConverter(objectMapper);
        // STY-948: Only use strings types as header.
        //
        // When using a complex objects in a header the DefaultKafkaHeaderMapper
        // will store the type of that object in separate header. The consumer
        // will then deserialize the received bytes to that type.
        // This is not secure because it allows the producer to trigger
        // deserialization an unsafe type.
        //
        // Instead, register a org.springframework.core.convert.converter.Converter
        // to convert headers to an object.
        SimpleKafkaHeaderMapper headerMapper = new SimpleKafkaHeaderMapper();
        headerMapper.setMapAllStringsOut(true);
        messageConverter.setHeaderMapper(headerMapper);
        return messageConverter;

    }

    @Bean
    @ConditionalOnMissingBean(JsonSerializer.class)
    @SuppressWarnings({"squid:S2095", "squid:S1452"}) // Indicates to spring that this can create any consumer factory
    public Serializer<?> kafkaJsonSerializer(ObjectMapper objectMapper) {
        // When `kafkaTemplate.send(topic, userId, message) is called with a regular object the json serializer
        // is needed to serialize the object.
        //
        // When `kafkaTemplate.send(topic, userId, message) is called with `org.springframework.messaging.Message` the
        // kafka template will use the `kafkaMessageConverter` to create the bytes. These no longer
        // need to be serialized.
        //
        // The BytesOrJsonSerializer can handle both scenarios. With the assumption that the bytes produced by the
        // `kafkaMessageConverter` message converter are valid json.
        final BytesSerializer bytesSerializer = new BytesSerializer();
        final JsonSerializer<?> jsonSerializer = new JsonSerializer<>(objectMapper);
        return new BytesOrJsonSerializer<>(bytesSerializer, jsonSerializer);
    }

    @Bean
    @ConditionalOnMissingBean(ProducerFactory.class)
    @SuppressWarnings("squid:S1452") // Indicates to spring that this can create any consumer factory
    public ProducerFactory<?, ?> kafkaProducerFactory(Serializer<?> kafkaJsonSerializer,
                                                      final Optional<VaultKafkaKeystoreInitializer> optionalVaultKafkaKeystoreInitializer) {
        final Map<String, Object> producerProperties = this.kafkaProperties.buildProducerProperties();
        // Adds Kafka SSL configuration
        optionalVaultKafkaKeystoreInitializer.ifPresent(
                vk -> producerProperties.putAll(vk.kafkaProperties())
        );
        DefaultKafkaProducerFactory<?, ?> factory = new DefaultKafkaProducerFactory<>(
                producerProperties,
                new StringSerializer(),
                kafkaJsonSerializer
        );

        String transactionIdPrefix = this.kafkaProperties.getProducer()
                .getTransactionIdPrefix();

        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }
        return factory;
    }

    @RequiredArgsConstructor
    private static class BytesOrJsonSerializer<T> implements Serializer<T> {
        private final BytesSerializer bytesSerializer;
        private final JsonSerializer<T> jsonSerializer;

        @Override
        public void configure(Map<String, ?> configs, boolean isKey) {
            bytesSerializer.configure(configs, isKey);
            jsonSerializer.configure(configs, isKey);
        }

        @Override
        public byte[] serialize(String topic, T data) {
            if (data instanceof Bytes) {
                Bytes bytes = (Bytes) data;
                return bytes.get();
            }
            return jsonSerializer.serialize(topic, data);
        }

        @Override
        public void close() {
            bytesSerializer.close();
            jsonSerializer.close();
        }
    }

    @RequiredArgsConstructor
    @Slf4j
    public static class KafkaProducerHealthIndicator extends AbstractHealthIndicator {
        private final String healthTopic;
        private final long timeout;
        private final Producer<String, String> producer;
        private final String applicationName;

        @Override
        protected void doHealthCheck(Health.Builder builder) {
            Status healthStatus;

            try {
                Clock clock = Clock.systemUTC();
                ZonedDateTime now = ZonedDateTime.now(clock);
                Future<RecordMetadata> sendFuture = producer.send(new ProducerRecord<>(healthTopic, now.toString(), applicationName));
                sendFuture.get(timeout, TimeUnit.MILLISECONDS);

                healthStatus = Status.UP;
            } catch (Exception ex) {
                log.error("Exception thrown while sending message to Kafka health topic", ex);

                healthStatus = Status.DOWN;
                builder.withException(ex);
            }

            builder.status(healthStatus);
        }
    }

}

