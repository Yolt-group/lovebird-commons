package nl.ing.lovebird.rest.deleteuser;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class DeleteUserAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DeleteUserAutoConfiguration.class));

    @Test
    void shouldCreateBeansIfUserDeleteIsOnClassPath() {
        contextRunner
                .run(context -> assertThat(context)
                        .hasSingleBean(DeleteUserAutoConfiguration.class)
                        .hasSingleBean(DeleteUserController.class)
                        .hasSingleBean(UserDeleter.class)
                );
    }

    @Test
    void shouldCreateReactiveControllerForReactiveContextIfUserDeleteIsOnClassPath() {
        new ReactiveWebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(DeleteUserAutoConfiguration.class))
                .run(context -> assertThat(context)
                        .hasSingleBean(DeleteUserAutoConfiguration.class)
                        .hasSingleBean(ReactiveDeleteUserController.class)
                        .doesNotHaveBean(DeleteUserController.class)
                        .hasSingleBean(UserDeleter.class)
                );
    }
}
