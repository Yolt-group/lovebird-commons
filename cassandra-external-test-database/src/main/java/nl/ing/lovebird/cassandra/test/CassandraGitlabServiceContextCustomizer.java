package nl.ing.lovebird.cassandra.test;

import lombok.EqualsAndHashCode;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;

@EqualsAndHashCode
public class CassandraGitlabServiceContextCustomizer implements ContextCustomizer {
    @Override
    public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        // no-op, cassandra credentials for the cassandra service in gitlab ci
        // can be provided by setting the properties in application-test.yml.
    }
}
