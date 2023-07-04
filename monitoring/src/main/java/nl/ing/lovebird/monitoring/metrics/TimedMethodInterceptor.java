package nl.ing.lovebird.monitoring.metrics;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Builder;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Pulled in from: https://github.com/micrometer-metrics/micrometer/pull/500/files
 * XXX Remove once using a micrometer version that includes the above MR
 */
public class TimedMethodInterceptor implements MethodInterceptor, MeterBinder {
    static final String DEFAULT_METRIC_NAME = "method_timed";

    private MeterRegistry registry;
    private final BiFunction<String, MethodInvocation, String> timedMetricNameResolver;
    private final Function<MethodInvocation, Iterable<Tag>> timedTagsResolver;

    public TimedMethodInterceptor() {
        this.timedMetricNameResolver = (metricName, invocation) -> metricName;
        this.timedTagsResolver = invocation -> Tags.of(
                "class", invocation.getMethod().getDeclaringClass().getSimpleName(),
                "method", invocation.getMethod().getName());
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (registry == null) {
            return invocation.proceed();
        }
        Method method = invocation.getMethod();
        Timed methodLevelTimed = AnnotatedElementUtils.findMergedAnnotation(method, Timed.class);
        Timed classLevelTimed = AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), Timed.class);
        if (methodLevelTimed == null) {
            method = ReflectionUtils.findMethod(invocation.getThis().getClass(), method.getName(), method.getParameterTypes());
            methodLevelTimed = AnnotatedElementUtils.findMergedAnnotation(method, Timed.class);
        }
        if (classLevelTimed == null && methodLevelTimed == null) {
            return invocation.proceed();
        }

        Timer.Sample sample = Timer.start(registry);
        final String metricName;
        if (methodLevelTimed != null && !methodLevelTimed.value().isEmpty()) {
            metricName = methodLevelTimed.value();
        } else if (classLevelTimed != null && !classLevelTimed.value().isEmpty()) {
            metricName = classLevelTimed.value();
        } else {
            metricName = DEFAULT_METRIC_NAME;
        }
        Builder builder = Timer.builder(timedMetricNameResolver.apply(metricName, invocation));
        if (methodLevelTimed != null) {
            builder.description(methodLevelTimed.description().isEmpty() ? null : methodLevelTimed.description())
                    .publishPercentileHistogram(methodLevelTimed.histogram())
                    .publishPercentiles(methodLevelTimed.percentiles().length == 0 ? null : methodLevelTimed.percentiles());
        } else {
            builder.description(classLevelTimed.description().isEmpty() ? null : classLevelTimed.description())
                    .publishPercentileHistogram(classLevelTimed.histogram())
                    .publishPercentiles(classLevelTimed.percentiles().length == 0 ? null : classLevelTimed.percentiles());
        }

        if (classLevelTimed != null) {
            builder.tags(classLevelTimed.extraTags());
        }
        if (methodLevelTimed != null) {
            builder.tags(methodLevelTimed.extraTags());
        }
        builder.tags(timedTagsResolver.apply(invocation));
        try {
            return invocation.proceed();
        } finally {
            sample.stop(builder.register(registry));
        }
    }
}
