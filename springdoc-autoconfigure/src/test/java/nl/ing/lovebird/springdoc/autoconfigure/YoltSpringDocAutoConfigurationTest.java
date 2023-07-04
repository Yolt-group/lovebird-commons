package nl.ing.lovebird.springdoc.autoconfigure;

import nl.ing.lovebird.springdoc.ExposeExternalApiPlugin;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.autoconfigure.AutoConfigurations.of;

class YoltSpringDocAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(of(
                    YoltSpringDocAutoConfiguration.class
            ));

    @Test
    void hasExposeExternalApiPluginBean() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(ExposeExternalApiPlugin.class));
    }

    @Test
    void exposeExternalApiPluginBeanConditionalOnOperationsCustomizer() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(OperationCustomizer.class))
                .run(context -> assertThat(context).doesNotHaveBean(ExposeExternalApiPlugin.class));
    }

}
