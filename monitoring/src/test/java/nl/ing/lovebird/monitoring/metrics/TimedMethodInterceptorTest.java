package nl.ing.lovebird.monitoring.metrics;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.Service;

import static org.assertj.core.api.Assertions.assertThat;

class TimedMethodInterceptorTest {

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @AfterEach
    void after() throws Exception {
        context.close();
    }

    private void bindRegistry() {
        MeterRegistry registry = context.getBean(MeterRegistry.class);
        TimedAnnotationAdvisor advisor = context.getBean(TimedAnnotationAdvisor.class);
        advisor.bindTo(registry);
    }

    @Test
    void testExplicitMetricName() {
        context.register(DefaultTimedMethodInterceptorConfig.class);
        context.refresh();

        bindRegistry();

        TimedService service = context.getBean(TimedService.class);
        MeterRegistry registry = context.getBean(MeterRegistry.class);

        service.timeWithExplicitValue();
        assertThat(registry.get("something")
            .tag("class", "TimedService")
            .tag("method", "timeWithExplicitValue")
            .tag("extra", "tag")
            .timer().count()).isEqualTo(1);
    }

    @Test
    void testDefaultMetricName() {
        context.register(DefaultTimedMethodInterceptorConfig.class);
        context.refresh();

        bindRegistry();

        TimedService service = context.getBean(TimedService.class);
        MeterRegistry registry = context.getBean(MeterRegistry.class);

        service.timeWithoutValue();
        assertThat(registry.get(TimedMethodInterceptor.DEFAULT_METRIC_NAME)
            .tag("class", "TimedService")
            .tag("method", "timeWithoutValue")
            .tag("extra", "tag")
            .timer().count()).isEqualTo(1);
    }

    @Test
    void testInterfaceMethodTimed() {
        context.register(DefaultTimedMethodInterceptorConfig.class);
        context.refresh();

        bindRegistry();

        TimedInterface service = context.getBean(TimedInterface.class);
        MeterRegistry registry = context.getBean(MeterRegistry.class);

        service.timeOnInterface();
        assertThat(registry.get(TimedMethodInterceptor.DEFAULT_METRIC_NAME)
            .tag("class", "TimedService")
            .tag("method", "timeOnInterface")
            .tag("extra", "tag")
            .timer().count()).isEqualTo(1);
    }

    @Test
    void testClassTimed() {
        context.register(DefaultTimedMethodInterceptorConfig.class);
        context.refresh();

        bindRegistry();

        AnnotatedTimedService service = context.getBean(AnnotatedTimedService.class);
        MeterRegistry registry = context.getBean(MeterRegistry.class);

        service.timeWithoutValue();
        assertThat(registry.get("class.invoke")
            .tag("class", "AnnotatedTimedService")
            .tag("method", "timeWithoutValue")
            .tag("extra", "tag")
            .timer().count()).isEqualTo(1);
    }

    @Test
    void testMethodLevelMetricName() {
        context.register(DefaultTimedMethodInterceptorConfig.class);
        context.refresh();

        bindRegistry();

        AnnotatedTimedService service = context.getBean(AnnotatedTimedService.class);
        MeterRegistry registry = context.getBean(MeterRegistry.class);

        service.timeWithMethodLevelName();
        assertThat(registry.get("my.method")
            .tag("class", "AnnotatedTimedService")
            .tag("method", "timeWithMethodLevelName")
            .tag("extra", "tag")
            .timer().count()).isEqualTo(1);
    }

    @Test
    void testMergedClassAndMethodTags() {
        context.register(DefaultTimedMethodInterceptorConfig.class);
        context.refresh();

        bindRegistry();

        AnnotatedTimedService service = context.getBean(AnnotatedTimedService.class);
        MeterRegistry registry = context.getBean(MeterRegistry.class);

        service.timeWithMergedTags();
        assertThat(registry.get("class.invoke")
            .tag("class", "AnnotatedTimedService")
            .tag("method", "timeWithMergedTags")
            .tag("extra", "tag")
            .tag("extra2", "tag")
            .timer().count()).isEqualTo(1);
    }

    @Configuration
    @EnableAspectJAutoProxy(proxyTargetClass = true)
    @Import({ TimedService.class, AnnotatedTimedService.class })
    static class DefaultTimedMethodInterceptorConfig {
        @Bean
        public SimpleMeterRegistry simpleMeterRegistry() {
            return new SimpleMeterRegistry();
        }

        @Bean
        public TimedMethodInterceptor timedMethodInterceptor() {
            return new TimedMethodInterceptor();
        }

        @Bean
        public TimedAnnotationAdvisor timedAnnotationAdvisor(TimedMethodInterceptor timedMethodInterceptor) {
            return new TimedAnnotationAdvisor(timedMethodInterceptor);
        }
    }

    interface TimedInterface {
        String timeWithExplicitValue();

        String timeWithoutValue();

        @Timed(extraTags = { "extra", "tag" })
        String timeOnInterface();
    }

    @Service
    static class TimedService implements TimedInterface {
        @Timed(value = "something", extraTags = { "extra", "tag" })
        @Override
        public String timeWithExplicitValue() {
            return "I'm";
        }

        @Timed(extraTags = { "extra", "tag" })
        @Override
        public String timeWithoutValue() {
            return "sorry";
        }

        @Override
        public String timeOnInterface() {
            return "Dave,";
        }
    }

    @Timed(value = "class.invoke", description = "class description", extraTags = { "extra", "tag" })
    @Service
    static class AnnotatedTimedService {

        public String timeWithoutValue() {
            return "I can't";
        }

        @Timed("my.method")
        public String timeWithMethodLevelName() {
            return "do";
        }

        @Timed(extraTags = { "extra2", "tag" })
        public String timeWithMergedTags() {
            return "that";
        }
    }
}