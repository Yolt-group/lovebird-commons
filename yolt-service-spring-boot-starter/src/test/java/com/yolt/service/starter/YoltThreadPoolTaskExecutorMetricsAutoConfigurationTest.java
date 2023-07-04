package com.yolt.service.starter;

import com.yolt.service.starter.metrics.YoltThreadPoolTaskExecutorMetricsAutoConfiguration.ThreadPoolTaskExecutorMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class YoltThreadPoolTaskExecutorMetricsAutoConfigurationTest {

    @Test
    void test() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.afterPropertiesSet();
        ThreadPoolTaskExecutorMetrics metrics = new ThreadPoolTaskExecutorMetrics(singletonMap("testExecutor", executor));
        assertDoesNotThrow(() -> metrics.bindTo(new SimpleMeterRegistry()));
    }
}
