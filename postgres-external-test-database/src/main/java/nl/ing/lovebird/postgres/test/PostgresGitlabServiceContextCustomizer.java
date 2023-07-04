package nl.ing.lovebird.postgres.test;

import lombok.EqualsAndHashCode;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
public class PostgresGitlabServiceContextCustomizer implements ContextCustomizer {
    @Override
    public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        MutablePropertySources sources = context.getEnvironment().getPropertySources();
        sources.addFirst(new MapPropertySource("Dynamic PostgresContainer Test Properties", buildDynamicPropertiesMap()));
    }

    private Map<String, Object> buildDynamicPropertiesMap() {
        Map<String, Object> properties = new HashMap<>();
        // Prevent any in memory db from replacing the data source
        // See @AutoConfigureTestDatabase
        properties.put("spring.test.database.replace", "NONE");
        // postgres credentials for the cassandra service in gitlab ci
        // can be provided by setting the properties in application-test.yml.
        return properties;
    }
}
