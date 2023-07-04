package com.yolt.service.starter.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@ConditionalOnClass(MeterRegistry.class)
@AutoConfiguration
@PropertySource("classpath:application-versions.properties")
public class YoltVersionMetricsAutoConfiguration {

    @Bean
    public LovebirdCommonsVersionMetric bind(@Value("${info.lovebird-commons}") String version) {
        return new LovebirdCommonsVersionMetric(version);
    }

    public static final class LovebirdCommonsVersionMetric implements MeterBinder {
        private final String version;

        public LovebirdCommonsVersionMetric(String version) {
            this.version = version;
        }

        @Override
        public void bindTo(MeterRegistry meterRegistry) {
            Gauge.builder("lovebird-commons", () -> 1)
                    .tag("version", version)
                    .description("The lovebird commons version")
                    .register(meterRegistry);
        }
    }
}
