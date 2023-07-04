package com.yolt.service.starter.web;

import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.errorhandling.ExceptionHandlingService;
import nl.ing.lovebird.errorhandling.config.BaseExceptionHandlers;
import nl.ing.lovebird.errorhandling.config.BaseServletExceptionHandlers;
import nl.ing.lovebird.errorhandling.config.BaseThrowableExceptionHandlers;
import nl.ing.lovebird.errorhandling.config.BaseThrowableWithSpringSecurityExceptionHandlers;
import nl.ing.lovebird.errorhandling.config.BaseTomcatExceptionHandlers;
import nl.ing.lovebird.errorhandling.config.BaseValidationExceptionHandlers;
import nl.ing.lovebird.errorhandling.config.BeanValidationLoggingConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

@ConditionalOnWebApplication
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(ErrorHandlingProperties.class)
public class ErrorHandlingAutoConfiguration {

    private final ErrorHandlingProperties properties;

    @Bean
    public ExceptionHandlingService exceptionHandlingService() {
        return new ExceptionHandlingService(properties.getPrefix());
    }

    @Bean
    @Order(value = Ordered.LOWEST_PRECEDENCE)
    @ConditionalOnMissingClass("org.springframework.security.access.AccessDeniedException")
    public BaseThrowableExceptionHandlers baseThrowableExceptionHandlers(ExceptionHandlingService service) {
        return new BaseThrowableExceptionHandlers(service);
    }

    @Bean
    @Order(value = Ordered.LOWEST_PRECEDENCE)
    @ConditionalOnClass(name = "org.springframework.security.access.AccessDeniedException")
    public BaseThrowableWithSpringSecurityExceptionHandlers baseThrowableWithSpringSecurityExceptionHandlers(ExceptionHandlingService service) {
        return new BaseThrowableWithSpringSecurityExceptionHandlers(service);
    }

    @Bean
    @Order(value = Ordered.LOWEST_PRECEDENCE - 1)
    public BaseExceptionHandlers baseExceptionHandlers(ExceptionHandlingService service) {
        return new BaseExceptionHandlers(service);
    }

    @Bean
    @Order(value = Ordered.LOWEST_PRECEDENCE - 2)
    @ConditionalOnClass(name = "org.apache.catalina.connector.ClientAbortException")
    public BaseTomcatExceptionHandlers baseTomcatExceptionHandlers(ExceptionHandlingService service) {
        return new BaseTomcatExceptionHandlers(service);
    }

    @Bean
    @Order(value = Ordered.LOWEST_PRECEDENCE - 3)
    @ConditionalOnWebApplication(type = SERVLET)
    public BaseServletExceptionHandlers baseServletExceptionHandlers(ExceptionHandlingService service) {
        return new BaseServletExceptionHandlers(service);
    }

    @Bean
    @Order(value = Ordered.LOWEST_PRECEDENCE - 4)
    @ConditionalOnClass(name = "javax.validation.ConstraintViolationException")
    public BaseValidationExceptionHandlers baseValidationExceptionHandlers(ExceptionHandlingService service) {
        return new BaseValidationExceptionHandlers(service);
    }

    @ConditionalOnWebApplication(type = SERVLET)
    @Import(BeanValidationLoggingConfiguration.class)
    public static class BeanValidationLogging {

    }

}
