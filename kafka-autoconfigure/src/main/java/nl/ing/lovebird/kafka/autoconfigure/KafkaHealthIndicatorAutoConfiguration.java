package nl.ing.lovebird.kafka.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Inspired on the old Spring Boot Kafka health indicator which got removed see
 * https://github.com/spring-projects/spring-boot/issues/14088 once added again this class can be removed the TLS
 * properties can be configure directly on the admin client through properties
 */
@ConditionalOnClass(value = KafkaAdmin.class)
@ConditionalOnEnabledHealthIndicator(value = "kafka")
@AutoConfiguration(before = HealthContributorAutoConfiguration.class, after = KafkaAutoConfiguration.class)
@Slf4j
public class KafkaHealthIndicatorAutoConfiguration {

    private final KafkaAdmin admin;
    private final Optional<VaultKafkaKeystoreInitializer> vaultKafkaKeystoreInitializer;

    public KafkaHealthIndicatorAutoConfiguration(KafkaAdmin admin, Optional<VaultKafkaKeystoreInitializer> vaultKafkaKeystoreInitializer) {
        this.admin = admin;
        this.vaultKafkaKeystoreInitializer = vaultKafkaKeystoreInitializer;
    }

    @Bean
    public AdminClient kafkaAdminClient() {
        Map<String, Object> properties = new HashMap<>(admin.getConfigurationProperties());
        // Adds Kafka SSL configuration
        vaultKafkaKeystoreInitializer.ifPresent(
                vk -> properties.putAll(vk.kafkaProperties())
        );
        return AdminClient.create(properties);
    }

    @Bean
    public HealthIndicator kafkaHealthIndicator() {
        log.info("Wiring Kafka health indicator");
        final DescribeClusterOptions describeClusterOptions = new DescribeClusterOptions().timeoutMs(1000);
        final AdminClient adminClient = kafkaAdminClient();
        return () -> {
            final DescribeClusterResult describeCluster = adminClient.describeCluster(describeClusterOptions);
            try {
                final String clusterId = describeCluster.clusterId().get();
                final int nodeCount = describeCluster.nodes().get().size();
                return Health.up()
                        .withDetail("clusterId", clusterId)
                        .withDetail("nodeCount", nodeCount)
                        .build();
            } catch (InterruptedException | ExecutionException e) {
                return Health.down()
                        .withException(e)
                        .build();
            }
        };
    }
}
