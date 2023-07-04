package nl.ing.lovebird.kafka.test;

import lombok.EqualsAndHashCode;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

import java.util.List;

@EqualsAndHashCode
public class KafkaExternalTestClusterContextCustomizerFactory implements ContextCustomizerFactory {

    private static boolean isAnnotatedWithEnableKafkaTestContainer(Class<?> testClass) {
        return (AnnotatedElementUtils.hasAnnotation(testClass, EnableExternalKafkaTestCluster.class));
    }

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        if (!isAnnotatedWithEnableKafkaTestContainer(testClass)) {
            return null;
        }
        if (CIConditions.isRunningInGitlabCI()) {
            return new KafkaGitlabServiceContextCustomizer();
        } else {
            return new KafkaTestContainerContextCustomizer();
        }
    }

}
