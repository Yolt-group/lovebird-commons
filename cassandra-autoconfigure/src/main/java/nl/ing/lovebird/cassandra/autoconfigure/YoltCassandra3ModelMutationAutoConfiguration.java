package nl.ing.lovebird.cassandra.autoconfigure;

import com.datastax.driver.core.Cluster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.cassandra.modelmutation.CassandraModelMutationApplier;
import nl.lovebird.cassandra.versioning.CassandraV3ModelMutationWaiter;
import nl.lovebird.cassandra.versioning.SeparateConnectionCassandraModelMutationWaiter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

@Conditional(YoltCassandra3ModelMutationAutoConfiguration.Condition.class)
@AutoConfiguration(before = YoltCassandra3AutoConfiguration.class, afterName = "nl.ing.lovebird.cassandra.spring.autoconfigure.YoltCassandra4ModelMutationAutoConfiguration")
@EnableConfigurationProperties(YoltCassandraUpdatesProperties.class)
@RequiredArgsConstructor
public class YoltCassandra3ModelMutationAutoConfiguration {

    private final YoltCassandraUpdatesProperties cassandraUpdatesProperties;

    @Bean
    @ConditionalOnMissingBean(CassandraModelMutationApplier.class)
    public CassandraModelMutationApplier waitForCassandraVersionChangesToBeAppliedByK8SJob(
            YoltCassandraProperties properties,
            Cluster.Builder clusterInitializer
    ) {
        SeparateConnectionCassandraModelMutationWaiter waiter = new SeparateConnectionCassandraModelMutationWaiter(
                "cassandraUpdates",
                properties.getKeyspaceName(),
                clusterInitializer,
                cassandraUpdatesProperties.getSecondsToWait()
        );
        return new WaitForCassandraVersionChangesToBeAppliedByK8SJob(waiter);
    }

    @RequiredArgsConstructor
    @Slf4j
    private static class WaitForCassandraVersionChangesToBeAppliedByK8SJob implements CassandraModelMutationApplier {

        private final SeparateConnectionCassandraModelMutationWaiter waiter;

        @Override
        public void afterPropertiesSet() {
            log.info("Blocking until cassandra updates are applied...");
            waiter.blockUntilApplied();
        }
    }

    public static class Condition extends AllNestedConditions {

        public Condition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnClass(Cluster.class)
        static class HasCassandraOnClasspath {
        }

        @ConditionalOnClass(CassandraV3ModelMutationWaiter.class)
        static class HasCassandraV3ModelMutationWaiterOnClasspath {
        }
    }
}
