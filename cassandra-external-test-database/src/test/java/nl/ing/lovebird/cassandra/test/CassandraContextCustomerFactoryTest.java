package nl.ing.lovebird.cassandra.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.test.context.ContextCustomizer;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class CassandraContextCustomerFactoryTest {

    final CassandraExternalTestDatabaseContextCustomizerFactory factory = new CassandraExternalTestDatabaseContextCustomizerFactory();

    @Test
    void shouldNotCreateCustomizer() {
        class ExampleTest {

        }
        ContextCustomizer contextCustomizer = factory.createContextCustomizer(ExampleTest.class, Collections.emptyList());
        assertThat(contextCustomizer).isNull();
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "GITLAB_CI", matches = "true")
    void shouldCreateCassandraTestContainerContextCustomizerLocally() {
        @EnableExternalCassandraTestDatabase
        class ExampleTest {

        }
        ContextCustomizer contextCustomizer = factory.createContextCustomizer(ExampleTest.class, Collections.emptyList());
        assertThat(contextCustomizer).isInstanceOf(CassandraTestContainerContextCustomizer.class);
        // See the contract ContextCustomizer
        ContextCustomizer secondContextCustomizer = factory.createContextCustomizer(ExampleTest.class, Collections.emptyList());
        assertThat(contextCustomizer)
                .isEqualTo(secondContextCustomizer)
                .hasSameHashCodeAs(secondContextCustomizer);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GITLAB_CI", matches = "true")
    void shouldCreateCassandraGitlabServiceContextCustomizerInCi() {
        @EnableExternalCassandraTestDatabase
        class ExampleTest {

        }
        ContextCustomizer contextCustomizer = factory.createContextCustomizer(ExampleTest.class, Collections.emptyList());
        assertThat(contextCustomizer).isInstanceOf(CassandraGitlabServiceContextCustomizer.class);
        // See the contract ContextCustomizer
        ContextCustomizer secondContextCustomizer = factory.createContextCustomizer(ExampleTest.class, Collections.emptyList());
        assertThat(contextCustomizer)
                .isEqualTo(secondContextCustomizer)
                .hasSameHashCodeAs(secondContextCustomizer);
    }

}
