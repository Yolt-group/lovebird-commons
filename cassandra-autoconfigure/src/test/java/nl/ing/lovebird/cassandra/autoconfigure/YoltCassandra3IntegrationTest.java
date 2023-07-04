package nl.ing.lovebird.cassandra.autoconfigure;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.utils.UUIDs;
import nl.ing.lovebird.cassandra.test.TestCassandraCluster;
import nl.lovebird.cassandra.versioning.AbstractCassandraModelMutationWaiter.FileExecutionStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.NamedContributors;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Run all integration tests against a single cassandra instance, this
 * speeds up the tests significantly.
 */
class YoltCassandra3IntegrationTest {

    private static Session session;
    private static String clusterHostName;
    private static Integer port;

    final ApplicationContextRunner baseContextRunner = new ApplicationContextRunner()
            .withPropertyValues(
                    "spring.data.cassandra.keyspace-name=test",
                    "spring.data.cassandra.port=" + port,
                    "spring.data.cassandra.contactPoints=" + clusterHostName);

    @BeforeAll
    static void createKeyspace() {
        Cluster cluster = TestCassandraCluster.provideCluster();
        clusterHostName = TestCassandraCluster.getClusterHost();
        port = TestCassandraCluster.getClusterPort();

        session = cluster.connect();
        session.execute("CREATE KEYSPACE IF NOT EXISTS test WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 1};");
        session.execute("CREATE TABLE IF NOT EXISTS test.modelmutation(hcpk text, filename text, time timeuuid, script text, result text, user text, forced boolean, PRIMARY KEY (hcpk, filename, time));");
    }

    @Nested
    class YoltCassandra3AutoConfigurationTest {

        final ApplicationContextRunner contextRunner = baseContextRunner
                .withConfiguration(AutoConfigurations.of(YoltCassandra3AutoConfiguration.class));

        @Test
        @DisplayName("[SHOULD NOT] create autoconfiguration bean [GIVEN] cassandra is not on the class path")
        void notEnabledShouldNotHaveBeanInContext() {
            contextRunner
                    .withClassLoader(new FilteredClassLoader(Cluster.class))
                    .run(c -> assertThat(c).doesNotHaveBean(YoltCassandra3AutoConfiguration.class));
        }

        @Test
        @DisplayName("[SHOULD] create autoconfiguration and session beans [GIVEN] cassandra is on the class path")
        void enabledShouldHaveBeanInContext() {
            contextRunner
                    .run(c -> {
                        assertThat(c).hasSingleBean(YoltCassandra3AutoConfiguration.class);
                        assertThat(c).hasSingleBean(Session.class);
                    });
        }
    }

    @Nested
    class YoltCassandra3ACodecAutoConfigurationTest {

        ApplicationContextRunner contextRunner = baseContextRunner
                .withConfiguration(AutoConfigurations.of(
                        YoltCassandra3AutoConfiguration.class,
                        YoltCassandra3CodecAutoConfiguration.class
                ));

        @Test
        @DisplayName("[SHOULD] register instant codec")
        void registerInstantCodec() {
            contextRunner.run(c -> assertThat(c.getBean(Cluster.class)
                    .getConfiguration()
                    .getCodecRegistry()
                    .codecFor(Instant.now()))
                    .isNotNull());
        }

    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class YoltCassandra3HealthContributorAutoConfigurationTest {

        final ApplicationContextRunner contextRunner = baseContextRunner
                .withConfiguration(AutoConfigurations.of(
                        YoltCassandra3AutoConfiguration.class,
                        YoltCassandra3HealthContributorAutoConfiguration.class
                ));

        @Mock
        ResultSet resultSet;
        @Mock
        Session session1;
        @Mock
        Session session2;

        @Test
        @DisplayName("[GIVEN] a connected cassandra sessions [SHOULD] provide a health contributor with status UP")
        void testWithOneCassandraSession() {
            contextRunner
                    .run(c -> {
                        YoltCassandra3HealthContributor contributor = (YoltCassandra3HealthContributor) c.getBean(HealthContributor.class);
                        Health health = contributor.getHealth(true);
                        assertThat(health.getStatus()).isEqualTo(Status.UP);
                    });
        }

        @Test
        @DisplayName("[GIVEN] a two connected cassandra sessions [SHOULD] provide a composite health contributor with status UP")
        void testWithTwoCassandraSessions() {
            when(session1.execute(any(Statement.class))).thenReturn(resultSet);
            when(session2.execute(any(Statement.class))).thenReturn(resultSet);

            contextRunner
                    .withBean("1", Session.class, () -> session1)
                    .withBean("2", Session.class, () -> session2)
                    .run(c -> {
                        CompositeHealthContributor contributors = (CompositeHealthContributor) c.getBean(HealthContributor.class);
                        contributors.forEach(healthContributorNamedContributor -> {
                            AbstractHealthIndicator contributor = (AbstractHealthIndicator) healthContributorNamedContributor.getContributor();
                            Health health = contributor.health();
                            assertThat(health.getStatus()).isEqualTo(Status.UP);
                        });
                    });
        }

        @Test
        @DisplayName("[GIVEN] a one connected and one disconnected cassandra sessions [SHOULD] provide a composite health contributor with one UP and one DOWN")
        void testWithTwoCassandraSessions_oneDown() {
            when(session1.execute(any(Statement.class))).thenReturn(resultSet);
            when(session2.execute(any(Statement.class))).thenThrow(new RuntimeException("Cassandra session2 is disconnected"));

            contextRunner
                    .withBean("1", Session.class, () -> session1)
                    .withBean("2", Session.class, () -> session2)
                    .run(c -> {
                        NamedContributors<?> contributors = (NamedContributors<?>) c.getBean(HealthContributor.class);
                        YoltCassandra3HealthContributor contributor1 = (YoltCassandra3HealthContributor) contributors.getContributor("1");

                        Health health = contributor1.getHealth(true);
                        assertThat(health.getStatus()).isEqualTo(Status.UP);

                        YoltCassandra3HealthContributor contributor2 = (YoltCassandra3HealthContributor) contributors.getContributor("2");
                        Health health2 = contributor2.getHealth(true);
                        assertThat(health2.getStatus()).isEqualTo(Status.DOWN);
                    });
        }
    }

    @Nested
    class YoltCassandra3ModelMutationAutoConfigurationTest {

        final ApplicationContextRunner context = baseContextRunner
                .withConfiguration(AutoConfigurations.of(
                        YoltCassandra3AutoConfiguration.class,
                        YoltCassandra3HealthContributorAutoConfiguration.class,
                        YoltCassandra3ModelMutationAutoConfiguration.class
                ));

        @Test
        void shouldNotBeReadyUntilAllMutationsAreApplied() {
            insertModelMutation(session, "1-foo.cql", FileExecutionStatus.OK);
            insertModelMutation(session, "2-bar.cql", FileExecutionStatus.SKIP);
            insertModelMutation(session, "3-foo.cql", FileExecutionStatus.OK);

            context.run(context -> assertThat(context.getBean(YoltCassandra3HealthContributor.class)
                    .health()
                    .getStatus())
                    .isEqualTo(Status.UP));
        }

        private void insertModelMutation(Session session, String fileName, FileExecutionStatus fileExecutionStatus) {
            session
                    .execute(QueryBuilder.insertInto("test", "modelmutation")
                            .value("hcpk", "HCPK")
                            .value("filename", fileName)
                            .value("time", UUIDs.timeBased())
                            .value("forced", false)
                            .value("result", fileExecutionStatus.toString()));
        }
    }
}
