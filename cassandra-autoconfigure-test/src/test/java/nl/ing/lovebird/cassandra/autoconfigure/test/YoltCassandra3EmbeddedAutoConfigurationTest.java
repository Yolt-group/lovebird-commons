package nl.ing.lovebird.cassandra.autoconfigure.test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import nl.ing.lovebird.cassandra.autoconfigure.YoltCassandra3AutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class YoltCassandra3EmbeddedAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("spring.data.cassandra.keyspace-name=foo")
            .withConfiguration(AutoConfigurations.of(
                    YoltCassandra3AutoConfiguration.class,
                    YoltCassandra3EmbeddedAutoConfiguration.class,
                    YoltCassandra3ModelMutationTestAutoConfiguration.class));

    @Test
    void shouldNotCreateIfCassandraIsNotOnTheClasspath() {
        contextRunner.withClassLoader(new FilteredClassLoader(Cluster.class))
                .run(context -> assertThat(context)
                        .doesNotHaveBean(Cluster.class)
                        .doesNotHaveBean(Session.class)
                );
    }
}
