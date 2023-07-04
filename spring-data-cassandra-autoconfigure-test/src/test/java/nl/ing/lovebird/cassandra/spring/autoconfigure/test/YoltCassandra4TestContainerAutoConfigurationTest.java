package nl.ing.lovebird.cassandra.spring.autoconfigure.test;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.auth.AuthProvider;
import nl.ing.lovebird.cassandra.modelmutation.CassandraModelMutationApplier;
import nl.ing.lovebird.cassandra.spring.autoconfigure.YoltCassandra4VaultAutoConfiguration;
import nl.ing.lovebird.cassandra.test.CassandraExternalTestDatabaseContextCustomizerFactory;
import nl.ing.lovebird.cassandra.test.EnableExternalCassandraTestDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.ContextCustomizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class YoltCassandra4TestContainerAutoConfigurationTest {

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
                    CassandraAutoConfiguration.class,
                    YoltCassandra4ModelMutationTestAutoConfiguration.class,
                    YoltCassandra4VaultAutoConfiguration.class));

    @Test
    void shouldNotCreateIfCassandraIsNotOnTheClasspath() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(CqlSession.class))
                .run(context -> assertThat(context)
                        .doesNotHaveBean(CqlSessionBuilderCustomizer.class)
                        .doesNotHaveBean(AuthProvider.class)
                        .doesNotHaveBean(CassandraModelMutationApplier.class)
                );
    }

    @Test
    void shouldCreateBeansIfCassandraContainerBeanIsAvailable() {
        contextRunner
                .withBean(CqlSessionBuilder.class, () -> Mockito.mock(CqlSessionBuilder.class, Mockito.RETURNS_DEEP_STUBS))
                .run(context -> assertThat(context)
                        .hasSingleBean(CqlSession.class)
                );
    }

    @Test
    void shouldCreateBeansIfCassandraContainerBeanIsAvailable2(@TempDir Path tempDir) throws IOException {
        Path secretsFile = tempDir.resolve("secrets");
        Files.createFile(secretsFile);

        contextRunner
                .withPropertyValues(
                        "yolt.vault.enabled=true",
                        "yolt.vault.cassandra.vault_creds_file=" + secretsFile.toAbsolutePath())
                .withBean(CqlSessionBuilder.class, () -> Mockito.mock(CqlSessionBuilder.class, Mockito.RETURNS_DEEP_STUBS))
                .run(context -> assertThat(context)
                                .hasSingleBean(CqlSession.class)
                                .hasBean("cassandra4AuthProviderVault")
                );
    }
}
