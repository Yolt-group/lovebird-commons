package nl.ing.lovebird.cassandra.test;

import lombok.EqualsAndHashCode;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.test.context.ContextConfigurationAttributes;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

import java.util.List;

@EqualsAndHashCode
public class CassandraExternalTestDatabaseContextCustomizerFactory implements ContextCustomizerFactory {

    private static boolean isAnnotatedWithEnableCassandraTestContainer(Class<?> testClass) {
        return (AnnotatedElementUtils.hasAnnotation(testClass, EnableExternalCassandraTestDatabase.class));
    }

    @Override
    public ContextCustomizer createContextCustomizer(Class<?> testClass, List<ContextConfigurationAttributes> configAttributes) {
        if (!isAnnotatedWithEnableCassandraTestContainer(testClass)) {
            return null;
        }
        if (CIConditions.isRunningInGitlabCI()) {
            return new CassandraGitlabServiceContextCustomizer();
        } else {
            return new CassandraTestContainerContextCustomizer();
        }
    }

}
