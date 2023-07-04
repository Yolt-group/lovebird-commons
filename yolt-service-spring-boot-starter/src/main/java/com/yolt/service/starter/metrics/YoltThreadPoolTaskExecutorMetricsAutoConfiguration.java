package com.yolt.service.starter.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;

import static io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics.monitor;

@AutoConfiguration
public class YoltThreadPoolTaskExecutorMetricsAutoConfiguration {

    @Bean
    public ThreadPoolTaskExecutorMetrics threadPoolTaskExecutorMetrics(Map<String, ThreadPoolTaskExecutor> executors){
        return new ThreadPoolTaskExecutorMetrics(executors);
    }

    @RequiredArgsConstructor
    public static class ThreadPoolTaskExecutorMetrics implements MeterBinder {

        private final Map<String, ThreadPoolTaskExecutor> executors;

        @Override
        public void bindTo(MeterRegistry registry) {
            executors.forEach((name, executor) -> monitor(registry, executor.getThreadPoolExecutor(), name, "spring"));
        }
    }
}
