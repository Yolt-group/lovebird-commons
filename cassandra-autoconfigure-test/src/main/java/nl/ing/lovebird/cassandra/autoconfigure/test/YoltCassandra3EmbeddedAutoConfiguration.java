package nl.ing.lovebird.cassandra.autoconfigure.test;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PlainTextAuthProvider;
import info.archinnov.achilles.embedded.CassandraEmbeddedConfigParameters;
import info.archinnov.achilles.embedded.CassandraEmbeddedServerBuilder;
import info.archinnov.achilles.embedded.CassandraShutDownHook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.cassandra.autoconfigure.YoltCassandra3AutoConfiguration;
import nl.ing.lovebird.cassandra.autoconfigure.YoltCassandraProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

import static nl.ing.lovebird.cassandra.autoconfigure.test.YoltCassandra3EmbeddedAutoConfiguration.EmbeddedCassandraCompositeConditions;

/**
 * This AutoConfiguration starts a Cassandra Cluster with the help of the achilles dependency.
 *
 * @deprecated Running Cassandra locally on jdk9+ has issues on Windows. Cassandra TestContainers is the replacement.
 * Other than that, the achilles project is not well maintained, so we want to get rid of it.
 */
@Deprecated
@Conditional(EmbeddedCassandraCompositeConditions.class)
@AutoConfiguration(before = YoltCassandra3AutoConfiguration.class)
@RequiredArgsConstructor
@Slf4j
public class YoltCassandra3EmbeddedAutoConfiguration {

    private final YoltCassandraProperties properties;

    @Bean
    public AuthProvider embeddedCassandraAuthProvider() {
        log.info("Overriding Cassandra credentials for embedded Cassandra");
        return new PlainTextAuthProvider("cassandra", "cassandra");
    }

    @Bean
    public Cluster embeddedCassandraCluster() {
        return CassandraEmbeddedServerBuilder
                .builder()
                .cleanDataFilesAtStartup(true)
                .withKeyspaceName(properties.getKeyspaceName())
                .withClusterName(CassandraEmbeddedConfigParameters.CLUSTER_NAME)
                // Adding a new CassandraShutDownHook will make the tests on Windows successfully exit.
                // Normally you would use this to control the shutdown of cassandra manually, but this does not seem needed...
                .withShutdownHook(new CassandraShutDownHook())
                .buildNativeCluster();
    }

    static class EmbeddedCassandraCompositeConditions extends AllNestedConditions {
        public EmbeddedCassandraCompositeConditions() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnClass(CassandraEmbeddedServerBuilder.class)
        static class HasAchillesJunit {
        }

        @ConditionalOnClass(Cluster.class)
        static class HasCassandraOnClasspath {
        }
    }
}
