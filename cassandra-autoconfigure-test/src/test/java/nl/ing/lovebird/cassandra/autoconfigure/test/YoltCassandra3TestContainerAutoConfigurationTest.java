package nl.ing.lovebird.cassandra.autoconfigure.test;

import com.datastax.driver.core.AuthProvider;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import nl.ing.lovebird.cassandra.autoconfigure.YoltCassandra3AutoConfiguration;
import nl.ing.lovebird.cassandra.modelmutation.CassandraModelMutationApplier;
import nl.ing.lovebird.cassandra.test.CassandraExternalTestDatabaseContextCustomizerFactory;
import nl.ing.lovebird.cassandra.test.EnableExternalCassandraTestDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.ContextCustomizer;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class YoltCassandra3TestContainerAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(
                    "spring.data.cassandra.contact-points=127.0.0.1",
                    "spring.data.cassandra.keyspace-name=foo",
                    "spring.data.cassandra.username=cassandra",
                    "spring.data.cassandra.password=cassandra"
            )
            .withInitializer(applicationContext -> {
                @EnableExternalCassandraTestDatabase
                class ExampleTest {

                }
                ContextCustomizer contextCustomizer = new CassandraExternalTestDatabaseContextCustomizerFactory()
                        .createContextCustomizer(ExampleTest.class, Collections.emptyList());
                contextCustomizer.customizeContext(applicationContext, null);
            })
            .withConfiguration(AutoConfigurations.of(
                    YoltCassandra3AutoConfiguration.class,
                    YoltCassandra3ModelMutationTestAutoConfiguration.class));

    @Test
    void shouldNotCreateIfCassandraIsNotOnTheClasspath() {
        contextRunner.withClassLoader(new FilteredClassLoader(Cluster.class))
                .run(context -> assertThat(context)
                        .doesNotHaveBean(Cluster.class)
                        .doesNotHaveBean(Session.class)
                        .doesNotHaveBean(CassandraModelMutationApplier.class)
                );
    }

    @Test
    void shouldCreateBeansIfCassandraContainerBeanIsAvailable() {
        contextRunner
                .run(context -> assertThat(context)
                        .hasSingleBean(Cluster.class)
                        .hasSingleBean(Session.class)
                        .hasSingleBean(AuthProvider.class)
                );
    }
}
