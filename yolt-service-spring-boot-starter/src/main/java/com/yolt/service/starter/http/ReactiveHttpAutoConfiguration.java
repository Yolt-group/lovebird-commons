package com.yolt.service.starter.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
import org.springframework.cloud.sleuth.autoconfig.SleuthBaggageProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@ConditionalOnClass(WebClient.class)
@AutoConfiguration
@Slf4j
public class ReactiveHttpAutoConfiguration {

    @Value("#{'${yolt.commons.additionalSensitiveHeaders:}'.replace(' ', '').split(',')}")
    private List<String> additionalSensitiveHeaders;

    @Bean
    @ConditionalOnProperty(name = "yolt.commons.block-sensitive-headers.enabled", matchIfMissing = true)
    public WebClientCustomizer webClientBlockSensitiveHeaders(@Value("${yolt.commons.block-sensitive-headers.dry-run:false}") boolean dryRun) {
        return webClientBuilder -> webClientBuilder.filter(new SensitiveHeaderFilter(additionalSensitiveHeaders, dryRun));
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.cloud.sleuth.autoconfig.SleuthBaggageProperties")
    @ConditionalOnProperty(name = "yolt.commons.block-sensitive-headers.enabled", matchIfMissing = true)
    public WebClientCustomizer webClientBlockSleuthPropagationKeys(@Autowired SleuthBaggageProperties sleuthProperties,
                                                                   @Value("${yolt.commons.block-sensitive-headers.dry-run:false}") boolean dryRun) {
        return webClientBuilder -> webClientBuilder.filter(new SensitiveHeaderFilter(sleuthProperties.getRemoteFields(), dryRun));
    }
}