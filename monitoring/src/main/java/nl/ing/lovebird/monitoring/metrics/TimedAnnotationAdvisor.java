package nl.ing.lovebird.monitoring.metrics;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.ComposablePointcut;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;

@RequiredArgsConstructor
@Getter
public class TimedAnnotationAdvisor extends AbstractPointcutAdvisor implements MeterBinder {
    private final Pointcut pointcut;
    private final transient TimedMethodInterceptor advice;

    public TimedAnnotationAdvisor(TimedMethodInterceptor timedMethodInterceptor) {
        Pointcut cpc = new AnnotationMatchingPointcut(Timed.class, true);
        Pointcut mpc = new AnnotationMatchingPointcut(null, Timed.class, true);
        this.pointcut = new ComposablePointcut(cpc).union(mpc);
        this.advice = timedMethodInterceptor;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.advice.bindTo(registry);
    }
}
