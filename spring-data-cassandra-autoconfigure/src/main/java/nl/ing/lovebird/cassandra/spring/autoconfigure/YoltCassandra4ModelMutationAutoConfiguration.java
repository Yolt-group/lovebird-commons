package nl.ing.lovebird.cassandra.spring.autoconfigure;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.cassandra.modelmutation.CassandraModelMutationApplier;
import nl.ing.lovebird.cassandra.spring.autoconfigure.YoltCassandra4ModelMutationAutoConfiguration.CassandraModelMutationApplierCqlSessionDependsOnPostProcessor;
import nl.ing.lovebird.cassandra.spring.versioning.CassandraV4ModelMutationWaiter;
import org.springframework.boot.autoconfigure.AbstractDependsOnBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CassandraProperties;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Conditional(YoltCassandra4ModelMutationAutoConfiguration.Condition.class)
@EnableConfigurationProperties(YoltCassandraUpdatesProperties.class)
@Import(CassandraModelMutationApplierCqlSessionDependsOnPostProcessor.class)
public class YoltCassandra4ModelMutationAutoConfiguration {

    private final YoltCassandraUpdatesProperties cassandraUpdatesProperties;

    public YoltCassandra4ModelMutationAutoConfiguration(CassandraProperties cassandraProperties, YoltCassandraUpdatesProperties yoltCassandraUpdatesProperties) {
        this.cassandraUpdatesProperties = yoltCassandraUpdatesProperties;
        if (cassandraProperties.getKeyspaceName() == null || cassandraProperties.getKeyspaceName().isEmpty()) {
            throw new IllegalArgumentException("Please set the keyspace name for the keyspace you're trying to connect to, via the property 'spring.data.cassandra.keyspace-name'");
        }
    }

    @Bean
    @ConditionalOnMissingBean(CassandraModelMutationApplier.class)
    public CassandraModelMutationApplier waitForCassandraVersionChangesToBeAppliedByK8SJob(CqlSessionBuilder sessionBuilder) {
        CassandraV4ModelMutationWaiter waiter = new CassandraV4ModelMutationWaiter(
                cassandraUpdatesProperties.getSecondsToWait(),
                sessionBuilder,
                "cassandraUpdates"
        );
        return new WaitForCassandraVersionChangesToBeAppliedByK8SJob(waiter);
    }

    @RequiredArgsConstructor
    @Slf4j
    private static class WaitForCassandraVersionChangesToBeAppliedByK8SJob implements CassandraModelMutationApplier {

        private final CassandraV4ModelMutationWaiter waiter;

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

        @ConditionalOnClass(CqlSession.class)
        static class HasCassandraOnClasspath {
        }

        @ConditionalOnClass(CassandraV4ModelMutationWaiter.class)
        static class HasCassandraV4ModelMutationWaiterOnClasspath {
        }
    }

    /**
     * This mechanism makes CqlSession dependent on CassandraModelMutationApplier bean.
     * This way we make sure that keyspace is initialized and cassandra updates are applied before main cql session is created.
     */
    public static class CassandraModelMutationApplierCqlSessionDependsOnPostProcessor extends CqlSessionDependsOnPostProcessor {
        CassandraModelMutationApplierCqlSessionDependsOnPostProcessor() {
            super(CassandraModelMutationApplier.class);
        }
    }

    public abstract static class CqlSessionDependsOnPostProcessor extends AbstractDependsOnBeanFactoryPostProcessor {

        private CqlSessionDependsOnPostProcessor(Class<?>... dependsOn) {
            super(CqlSession.class, dependsOn);
        }
    }
}
