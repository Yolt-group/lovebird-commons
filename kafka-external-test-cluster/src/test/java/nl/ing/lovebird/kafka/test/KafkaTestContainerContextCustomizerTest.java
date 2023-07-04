package nl.ing.lovebird.kafka.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextCustomizer;

import static org.assertj.core.api.Assertions.assertThat;

@DisabledIfEnvironmentVariable(named = "GITLAB_CI", matches = "true", disabledReason = "Test containers are not supported in CI")
class KafkaTestContainerContextCustomizerTest {

    @Test
    void shouldLaunchKafkaTestContainer() {
        new ApplicationContextRunner()
                .withInitializer(applicationContext -> {
                    ContextCustomizer contextCustomizer = new KafkaTestContainerContextCustomizer();
                    contextCustomizer.customizeContext(applicationContext, null);
                })
                .withConfiguration(AutoConfigurations.of(
                        KafkaAutoConfiguration.class
                ))
                .run(context -> assertThat(context)
                        .hasSingleBean(KafkaTemplate.class));
    }
}
