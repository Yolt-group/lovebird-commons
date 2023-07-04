package nl.ing.lovebird.kafka.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.test.context.ContextCustomizer;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaContextCustomerFactoryTest {

    final KafkaExternalTestClusterContextCustomizerFactory factory = new KafkaExternalTestClusterContextCustomizerFactory();

    @Test
    void shouldNotCreateCustomizer() {
        class ExampleTest {

        }
        ContextCustomizer contextCustomizer = factory.createContextCustomizer(ExampleTest.class, Collections.emptyList());
        assertThat(contextCustomizer).isNull();
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "GITLAB_CI", matches = "true")
    void shouldCreateKafkaTestContainerContextCustomizerLocally() {
        @EnableExternalKafkaTestCluster
        class ExampleTest {

        }
        ContextCustomizer contextCustomizer = factory.createContextCustomizer(ExampleTest.class, Collections.emptyList());
        assertThat(contextCustomizer).isInstanceOf(KafkaTestContainerContextCustomizer.class);
        // See the contract ContextCustomizer
        ContextCustomizer secondContextCustomizer = factory.createContextCustomizer(ExampleTest.class, Collections.emptyList());
        assertThat(contextCustomizer)
                .isEqualTo(secondContextCustomizer)
                .hasSameHashCodeAs(secondContextCustomizer);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "GITLAB_CI", matches = "true")
    void shouldCreateKafkaGitlabServiceContextCustomizerInCi() {
        @EnableExternalKafkaTestCluster
        class ExampleTest {

        }
        ContextCustomizer contextCustomizer = factory.createContextCustomizer(ExampleTest.class, Collections.emptyList());
        assertThat(contextCustomizer).isInstanceOf(KafkaGitlabServiceContextCustomizer.class);
        // See the contract ContextCustomizer
        ContextCustomizer secondContextCustomizer = factory.createContextCustomizer(ExampleTest.class, Collections.emptyList());
        assertThat(contextCustomizer)
                .isEqualTo(secondContextCustomizer)
                .hasSameHashCodeAs(secondContextCustomizer);
    }

}
