package nl.ing.lovebird.postgres.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.test.context.ContextCustomizer;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalPostgresContextCustomerFactoryTest {

    final ExternalPostgresTestDatabaseContextCustomizerFactory factory = new ExternalPostgresTestDatabaseContextCustomizerFactory();

    @Test
    void shouldNotCreateCustomizer() {
        class ExampleTest {

        }
        ContextCustomizer contextCustomizer = factory.createContextCustomizer(ExampleTest.class, Collections.emptyList());
        assertThat(contextCustomizer).isNull();
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "GITLAB_CI", matches = "true")
    void shouldCreatePostgresTestContainerContextCustomizerLocally() {
        @EnableExternalPostgresTestDatabase
        class ExampleTest {

        }
        ContextCustomizer contextCustomizer = factory.createContextCustomizer(ExampleTest.class, Collections.emptyList());
        assertThat(contextCustomizer).isInstanceOf(PostgresTestContainerContextCustomizer.class);
        // See the contract ContextCustomizer
        ContextCustomizer secondContextCustomizer = factory.createContextCustomizer(ExampleTest.class, Collections.emptyList());
        assertThat(contextCustomizer)
                .isEqualTo(secondContextCustomizer)
                .hasSameHashCodeAs(secondContextCustomizer);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GITLAB_CI", matches = "true")
    void shouldCreatePostgresGitlabServiceContextCustomizerInCi() {
        @EnableExternalPostgresTestDatabase
        class ExampleTest {

        }
        ContextCustomizer contextCustomizer = factory.createContextCustomizer(ExampleTest.class, Collections.emptyList());
        assertThat(contextCustomizer).isInstanceOf(PostgresGitlabServiceContextCustomizer.class);
        // See the contract ContextCustomizer
        ContextCustomizer secondContextCustomizer = factory.createContextCustomizer(ExampleTest.class, Collections.emptyList());
        assertThat(contextCustomizer)
                .isEqualTo(secondContextCustomizer)
                .hasSameHashCodeAs(secondContextCustomizer);
    }

}
