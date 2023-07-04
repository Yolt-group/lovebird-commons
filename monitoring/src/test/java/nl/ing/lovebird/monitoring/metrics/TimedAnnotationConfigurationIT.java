package nl.ing.lovebird.monitoring.metrics;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TimedAnnotationConfigurationIT {
    @Autowired
    private TimedService service;
    @Autowired
    private MeterRegistry registry;

    @Test
    void test() {
        service.doSomething();
        assertThat(registry.get("something")
                .tag("class", "TimedService")
                .tag("method", "doSomething")
                .timer().count()).isEqualTo(1);
    }
}

@SpringBootApplication
class TestApp { // Limit component scanning
    @Bean
    public TimedAnnotationAdvisor timedAnnotationAdvisor() {
        return new TimedAnnotationAdvisor(new TimedMethodInterceptor());
    }

    @Bean
    public TimedService timedService() {
        return new TimedService();
    }
}

class TimedService {
    @Timed("something")
    public void doSomething() {
        // Annotation only
    }
}
