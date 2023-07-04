package com.yolt.service.starter.http;

import com.yolt.service.starter.web.ErrorHandlingAutoConfiguration;
import nl.ing.lovebird.errorhandling.config.BaseExceptionHandlers;
import nl.ing.lovebird.errorhandling.ExceptionHandlingService;
import nl.ing.lovebird.errorhandling.config.BaseServletExceptionHandlers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.reactive.HandlerResult;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionHandlingAutoConfigurationTest {

    @Test
    void shouldNotCreateIfNotWebApplication() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ErrorHandlingAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader(GenericWebApplicationContext.class, HandlerResult.class))
                .run(context -> assertThat(context)
                .doesNotHaveBean(ExceptionHandlingService.class)
                .doesNotHaveBean(BaseExceptionHandlers.class)
                .doesNotHaveBean(BaseServletExceptionHandlers.class));

        new ReactiveWebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ErrorHandlingAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader(GenericWebApplicationContext.class, HandlerResult.class))
                .run(context -> assertThat(context)
                .doesNotHaveBean(ExceptionHandlingService.class)
                .doesNotHaveBean(BaseExceptionHandlers.class)
                .doesNotHaveBean(BaseServletExceptionHandlers.class));
    }

    @Test
    void shouldCreateForServletApplication() {
        new WebApplicationContextRunner()
                .withPropertyValues("yolt.commons.error-handling.prefix=TEST")
                .withConfiguration(AutoConfigurations.of(ErrorHandlingAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader(HandlerResult.class))
                .run(context -> assertThat(context)
                .hasSingleBean(ExceptionHandlingService.class)
                .hasSingleBean(BaseExceptionHandlers.class)
                .hasSingleBean(BaseServletExceptionHandlers.class));
    }
    @Test
    void shouldCreateForReactiveApplication() {
        new ReactiveWebApplicationContextRunner()
                .withPropertyValues("yolt.commons.error-handling.prefix=TEST")
                .withConfiguration(AutoConfigurations.of(ErrorHandlingAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader(GenericWebApplicationContext.class))
                .run(context -> assertThat(context)
                .hasSingleBean(ExceptionHandlingService.class)
                .hasSingleBean(BaseExceptionHandlers.class)
                .doesNotHaveBean(BaseServletExceptionHandlers.class));
    }
}
