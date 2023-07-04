package com.yolt.service.starter;

import com.yolt.service.starter.threadpool.CustomThreadPoolTaskExecutor;
import com.yolt.service.starter.threadpool.CustomThreadPoolTaskExecutorComponent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.annotation.UserConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;

class YoltTaskExecutorShutdownAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(YoltTaskExecutorShutdownAutoConfiguration.class));

    @Test
    void should_make_all_existing_executors_life_cycle_aware() {
        contextRunner
                .withBean("a", ThreadPoolTaskExecutor.class, ThreadPoolTaskExecutor::new)
                .withBean("b", Executor.class, ThreadPoolTaskExecutor::new)
                .withBean("c", ThreadPoolTaskScheduler.class, ThreadPoolTaskScheduler::new)
                .withBean("d", Executor.class, ThreadPoolTaskScheduler::new)
                .run(c -> {
                    assertThat(c).hasBean("a_smartLifecycle");
                    assertThat(c).hasBean("b_smartLifecycle");
                    assertThat(c).hasBean("c_smartLifecycle");
                    assertThat(c).hasBean("d_smartLifecycle");
                });
    }

    @Test
    void should_decorate_bean_from_configuration() {
        contextRunner
                .withUserConfiguration(BeanMethodConfiguration.class)
                .run(c -> assertThat(c).hasBean("a_smartLifecycle"));
    }

    @Test
    void should_decorate_subclasses() {
        contextRunner
                .withBean("a", CustomThreadPoolTaskExecutor.class, CustomThreadPoolTaskExecutor::new)
                .run(c -> assertThat(c).hasBean("a_smartLifecycle"));
    }

    @Test
    void should_decorate_application_task_executor_bean() {
        contextRunner
                .withConfiguration(AutoConfigurations.of(TaskExecutionAutoConfiguration.class))
                .run(c -> assertThat(c).hasBean(APPLICATION_TASK_EXECUTOR_BEAN_NAME + "_smartLifecycle"));
    }

    @Test
    void should_decorate_component() {
        contextRunner
                .withConfiguration(UserConfigurations.of(ComponentScanConfiguration.class))
                .run(c -> assertThat(c).hasBean("customThreadPoolTaskExecutorComponent_smartLifecycle"));
    }

    @Test
    void should_decorate_component_with_value() {
        contextRunner
                .withConfiguration(UserConfigurations.of(ComponentScanConfiguration.class))
                .withPropertyValues("com.example.value", "example")
                .run(c -> assertThat(c).hasBean("customThreadPoolTaskExecutorWithPropertiesComponent_smartLifecycle"));
    }

    @Test
    void closing_the_application_context_should_shutdown_the_thread_pool_twice() {
        ThreadPoolTaskExecutor executor = Mockito.spy(new ThreadPoolTaskExecutor());
        contextRunner
                .withBean("a", ThreadPoolTaskExecutor.class, () -> executor)
                .run(c -> {
                    verify(executor, Mockito.times(0)).shutdown();
                    c.close();
                    // Once via the SmartLifecycleWrapper and once via DisposableBean
                    verify(executor, Mockito.times(2)).shutdown();
                });
    }

    @Configuration
    public static class BeanMethodConfiguration {

        @Bean
        public ThreadPoolTaskExecutor a() {
            return new ThreadPoolTaskExecutor();
        }
    }

    @Configuration
    @ComponentScan(basePackageClasses = CustomThreadPoolTaskExecutorComponent.class)
    public static class ComponentScanConfiguration {

    }

}
