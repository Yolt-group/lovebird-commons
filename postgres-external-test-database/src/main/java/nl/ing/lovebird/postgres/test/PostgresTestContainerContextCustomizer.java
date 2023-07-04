package nl.ing.lovebird.postgres.test;

import lombok.EqualsAndHashCode;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
public class PostgresTestContainerContextCustomizer implements ContextCustomizer {

    @Override
    public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        PostgreSQLContainer<?> postgresContainer = PostgresTestContainerSingleton.instance();
        MutablePropertySources sources = context.getEnvironment().getPropertySources();
        sources.addFirst(new MapPropertySource("Dynamic PostgresContainer Test Properties", buildDynamicPropertiesMap(postgresContainer)));
    }


    private Map<String, Object> buildDynamicPropertiesMap(PostgreSQLContainer<?> postgresContainer) {
        Map<String, Object> properties = new HashMap<>();

        properties.put("spring.datasource.url", postgresContainer.getJdbcUrl());
        properties.put("spring.datasource.username", postgresContainer.getUsername());
        properties.put("spring.datasource.password", postgresContainer.getPassword());
        // Prevent any in memory db from replacing the data source
        // See @AutoConfigureTestDatabase
        properties.put("spring.test.database.replace", "NONE");
        return properties;
    }
}
