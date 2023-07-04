package com.yolt.service.starter;

import nl.ing.lovebird.monitoring.metrics.TimedAnnotationAdvisor;
import nl.ing.lovebird.monitoring.metrics.TimedMethodInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(name = "yolt.commons.monitoring.enabled", matchIfMissing = true)
@AutoConfiguration
public class MonitoringAutoConfiguration {
    @Bean
    @ConditionalOnProperty("yolt.commons.timedaspect.enabled")
    @ConditionalOnMissingBean(TimedAnnotationAdvisor.class)
    // Not (yet) enabled by default; CassandraRepository is annotated with @Timed, which showed little to no performance impact
    public TimedAnnotationAdvisor timedAnnotationAdvisor() {
        TimedMethodInterceptor timedMethodInterceptor = new TimedMethodInterceptor();
        return new TimedAnnotationAdvisor(timedMethodInterceptor);
    }
}
