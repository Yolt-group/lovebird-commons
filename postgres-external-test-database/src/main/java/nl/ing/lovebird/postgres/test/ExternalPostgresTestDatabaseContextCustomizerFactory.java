package nl.ing.lovebird.postgres.test;

import lombok.EqualsAndHashCode;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

import java.util.List;

@EqualsAndHashCode
public class ExternalPostgresTestDatabaseContextCustomizerFactory implements ContextCustomizerFactory {

    private static boolean isAnnotatedWithEnablePostgresTestContainer(Class<?> testClass) {
        return (AnnotatedElementUtils.hasAnnotation(testClass, EnableExternalPostgresTestDatabase.class));
    }

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        if (!isAnnotatedWithEnablePostgresTestContainer(testClass)) {
            return null;
        }
        if (CIConditions.isRunningInGitlabCI()) {
            return new PostgresGitlabServiceContextCustomizer();
        } else {
            return new PostgresTestContainerContextCustomizer();
        }
    }

}
