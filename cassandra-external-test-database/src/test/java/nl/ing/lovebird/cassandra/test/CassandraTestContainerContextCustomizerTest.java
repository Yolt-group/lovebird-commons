package nl.ing.lovebird.cassandra.test;

import com.datastax.oss.driver.api.core.CqlSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.ContextCustomizer;

import static org.assertj.core.api.Assertions.assertThat;

@DisabledIfEnvironmentVariable(named = "GITLAB_CI", matches = "true", disabledReason = "Test containers are not supported in CI")
class CassandraTestContainerContextCustomizerTest {

    @Test
    void shouldLaunchCassandraTestContainer() {
        new ApplicationContextRunner()
                .withInitializer(applicationContext -> {
                    ContextCustomizer contextCustomizer = new CassandraTestContainerContextCustomizer();
                    contextCustomizer.customizeContext(applicationContext, null);
                })
                .withConfiguration(AutoConfigurations.of(
                        CassandraAutoConfiguration.class,
                        CassandraDataAutoConfiguration.class
                ))
                .run(context -> assertThat(context)
                        .hasSingleBean(CqlSession.class));
    }
}
