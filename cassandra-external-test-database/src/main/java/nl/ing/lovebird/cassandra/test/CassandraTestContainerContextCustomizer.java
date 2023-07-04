package nl.ing.lovebird.cassandra.test;

import com.datastax.driver.core.Host;
import lombok.EqualsAndHashCode;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.testcontainers.containers.CassandraContainer;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
public class CassandraTestContainerContextCustomizer implements ContextCustomizer {

    @Override
    public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        CassandraContainer<?> cassandraContainer = CassandraTestContainerSingleton.instance();
        MutablePropertySources sources = context.getEnvironment().getPropertySources();
        sources.addFirst(new MapPropertySource("Dynamic CassandraContainer Test Properties", buildDynamicPropertiesMap(cassandraContainer)));
    }


    private Map<String, Object> buildDynamicPropertiesMap(CassandraContainer<?> cassandraContainer) {
        Map<String, Object> properties = new HashMap<>();

        String datacenter = cassandraContainer.getCluster().getMetadata().getAllHosts().stream().findFirst().map(Host::getDatacenter).orElse("datacenter1");
        properties.put("spring.data.cassandra.port", cassandraContainer.getFirstMappedPort());
        properties.put("spring.data.cassandra.contact-points", cassandraContainer.getHost());
        properties.put("spring.data.cassandra.localDatacenter", datacenter);
        return properties;
    }
}
