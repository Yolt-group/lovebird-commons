package com.yolt.service.starter.http;

import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.errorhandling.UserNotFoundResponseErrorHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.cloud.sleuth.autoconfig.SleuthBaggageProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(RestTemplate.class)
public class HttpAutoConfiguration {

    @Value("#{'${yolt.commons.additionalSensitiveHeaders:}'.replace(' ', '').split(',')}")
    private List<String> additionalSensitiveHeaders;

    @Bean
    @ConditionalOnProperty(name = "yolt.commons.block-sensitive-headers.enabled", matchIfMissing = true)
    public RestTemplateCustomizer restTemplateBlockSensitiveHeaders() {
        return restTemplate -> restTemplate.getInterceptors().add(new SensitiveHeaderInterceptor(additionalSensitiveHeaders));
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.cloud.sleuth.autoconfig.SleuthBaggageProperties")
    @ConditionalOnProperty(name = "yolt.commons.block-sensitive-headers.enabled", matchIfMissing = true)
    public RestTemplateCustomizer restTemplateBlockSleuthPropagationKeys(SleuthBaggageProperties sleuthProperties) {
        return restTemplate -> restTemplate.getInterceptors().add(new SensitiveHeaderInterceptor(sleuthProperties.getRemoteFields()));
    }

    /**
     * Enables forwarding of the user not found status code.
     *
     * @return rest template customizer
     * @see UserNotFoundResponseErrorHandler
     */
    @Bean
    @ConditionalOnProperty(name = "yolt.commons.forward-user-not-found-exception", havingValue = "true")
    public RestTemplateCustomizer forwardUserNotFoundException() {
        return restTemplate -> restTemplate.setErrorHandler(new UserNotFoundResponseErrorHandler());
    }
}