package com.yolt.service.starter.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class YoltVersionMetricsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    MetricsAutoConfiguration.class,
                    CompositeMeterRegistryAutoConfiguration.class,
                    YoltVersionMetricsAutoConfiguration.class
            ));

    @Test
    void testVersionsContributor() {
        contextRunner.run(context -> {
            assertDoesNotThrow(() -> context.getBean(MeterRegistry.class)
                    .find("lovebird-commons")
                    .tagKeys("version")
                    .gauge());
        });
    }
}
