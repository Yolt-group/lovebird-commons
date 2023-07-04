package nl.ing.lovebird.kafka.test;

import lombok.EqualsAndHashCode;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.MergedContextConfiguration;
import org.testcontainers.containers.KafkaContainer;

import java.util.HashMap;
import java.util.Map;

@EqualsAndHashCode
public class KafkaTestContainerContextCustomizer implements ContextCustomizer {

    @Override
    public void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        KafkaContainer kafkaContainer = KafkaTestContainerSingleton.instance();
        MutablePropertySources sources = context.getEnvironment().getPropertySources();
        sources.addFirst(new MapPropertySource("Dynamic KafkaContainer Test Properties", buildDynamicPropertiesMap(kafkaContainer)));
    }

    private Map<String, Object> buildDynamicPropertiesMap(KafkaContainer kafkaContainer) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
        return properties;
    }
}
