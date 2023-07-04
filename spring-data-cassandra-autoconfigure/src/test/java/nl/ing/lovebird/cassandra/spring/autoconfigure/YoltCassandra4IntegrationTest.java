package nl.ing.lovebird.cassandra.spring.autoconfigure;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.utils.UUIDs;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.auth.AuthProvider;
import nl.ing.lovebird.cassandra.modelmutation.CassandraModelMutationApplier;
import nl.ing.lovebird.cassandra.spring.versioning.CassandraV4ModelMutationWaiter.FileExecutionStatus;
import nl.ing.lovebird.cassandra.test.TestCassandraCluster;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Run all integration tests against a single cassandra instance, this
 * speeds up the tests significantly.
 */
class YoltCassandra4IntegrationTest {

    private static Session session;
    private static String clusterHostName;
    private static Integer port;

    final ApplicationContextRunner baseContextRunner = new ApplicationContextRunner()
            .withPropertyValues(
                    "spring.data.cassandra.keyspace-name=test",
                    "spring.data.cassandra.local-datacenter=datacenter1",
                    "spring.data.cassandra.port=" + port,
                    "spring.data.cassandra.contactPoints=" + clusterHostName);

    @BeforeAll
    static void setup() {
        Cluster cluster = TestCassandraCluster.provideCluster();
        clusterHostName = TestCassandraCluster.getClusterHost();
        port = TestCassandraCluster.getClusterPort();

        session = cluster.connect();
        session.execute("CREATE KEYSPACE IF NOT EXISTS test WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 1};");
        session.execute("CREATE TABLE IF NOT EXISTS test.modelmutation(hcpk text, filename text, time timeuuid, script text, result text, user text, forced boolean, PRIMARY KEY (hcpk, filename, time));");
    }

    @Nested
    class YoltCassandra4AutoConfigurationTest {

        final ApplicationContextRunner contextRunner = baseContextRunner
                .withConfiguration(AutoConfigurations.of(
                        CassandraAutoConfiguration.class,
                        YoltCassandra4ModelMutationAutoConfiguration.class,
                        YoltCassandra4VaultAutoConfiguration.class
                ));

        @Test
        @DisplayName("[SHOULD NOT] create autoconfiguration bean [GIVEN] cassandra is not on the class path")
        void test1() {
            contextRunner
                    .withClassLoader(new FilteredClassLoader(CqlSession.class))
                    .run(context -> assertThat(context)
                            .doesNotHaveBean(YoltCassandra4ModelMutationAutoConfiguration.class));
        }

        @Test
        @DisplayName("[SHOULD] create autoconfiguration and session beans [GIVEN] cassandra is on the class path")
        void test2() {
            contextRunner
                    .withBean(CassandraModelMutationApplier.class, () -> () -> {})
                    .run(context -> assertThat(context)
                            .hasSingleBean(YoltCassandra4ModelMutationAutoConfiguration.class)
                            .hasSingleBean(CqlSession.class));
        }

        @Test
        @DisplayName("[SHOULD] fail to start [GIVEN] cassandra is on the class path and keyspace name is not configured")
        void test3() {
            contextRunner
                    .withPropertyValues("spring.data.cassandra.keyspace-name=")
                    .withBean(CassandraModelMutationApplier.class, () -> () -> {})
                    .run(context -> assertThat(context.getStartupFailure().getCause().getCause())
                            .isInstanceOf(IllegalArgumentException.class)
                            .hasMessageContaining("Please set the keyspace name for the keyspace you're trying to connect to, via the property 'spring.data.cassandra.keyspace-name'"));
        }

        @Test
        @DisplayName("[SHOULD] create autoconfiguration, session, vault AuthProvider beans [GIVEN] cassandra is on the class path and vault is enabled")
        void test4(@TempDir Path tempDir) throws Exception {
            Path secretsFile = tempDir.resolve("secrets");
            Properties properties = new Properties();
            properties.setProperty("username", "test_username");
            properties.setProperty("password", "test_password");

            try (final OutputStream outputstream = new FileOutputStream(secretsFile.toFile())) {
                properties.store(outputstream, "credentials stored");
            }

            contextRunner
                    .withPropertyValues(
                            "yolt.vault.enabled=true",
                            "yolt.vault.cassandra.vault_creds_file=" + secretsFile.toAbsolutePath())
                    .withBean(CassandraModelMutationApplier.class, () -> () -> {})
                    .run(context -> assertThat(context)
                            .hasSingleBean(YoltCassandra4ModelMutationAutoConfiguration.class)
                            .hasSingleBean(CqlSession.class)
                            .hasBean("cassandra4AuthProviderVault")
                            .hasSingleBean(AuthProvider.class));
        }
    }

    @Nested
    class YoltCassandra4ModelMutationAutoConfigurationTest {

        final ApplicationContextRunner context = baseContextRunner
                .withConfiguration(AutoConfigurations.of(
                        CassandraAutoConfiguration.class,
                        YoltCassandra4ModelMutationAutoConfiguration.class
                ));

        private void insertModelMutation(Session session, String fileName, FileExecutionStatus fileExecutionStatus) {
            session
                    .execute(QueryBuilder.insertInto("test", "modelmutation")
                            .value("hcpk", "HCPK")
                            .value("filename", fileName)
                            .value("time", UUIDs.timeBased())
                            .value("forced", false)
                            .value("result", fileExecutionStatus.toString()));
        }

        @Test
        @DisplayName("[SHOULD] create CassandraV4ModelMutationApplier and YoltCassandra4ModelMutationAutoConfiguration beans and succeed starting context")
        void test1() {
            // If mutations are not inserted context will fail waiting k8s to insert them
            insertModelMutation(session, "1-foo.cql", FileExecutionStatus.OK);
            insertModelMutation(session, "2-bar.cql", FileExecutionStatus.SKIP);
            insertModelMutation(session, "3-foo.cql", FileExecutionStatus.OK);

            context.run(context -> assertThat(context)
                    .hasSingleBean(CassandraModelMutationApplier.class)
                    .hasSingleBean(YoltCassandra4ModelMutationAutoConfiguration.class));
        }

        @Test
        @DisplayName("[SHOULD NOT] create YoltCassandra4VersioningAutoConfiguration, [GIVEN] cassandra driver is not on the classpath")
        void test4() {
            context
                    .withClassLoader(new FilteredClassLoader(CqlSession.class))
                    .run(context -> assertThat(context)
                            .doesNotHaveBean(YoltCassandra4ModelMutationAutoConfiguration.class)
                            .doesNotHaveBean(CassandraModelMutationApplier.class)
                    );
        }
    }
}
