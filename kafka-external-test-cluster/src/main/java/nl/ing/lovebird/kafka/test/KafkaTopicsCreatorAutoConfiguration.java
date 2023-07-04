package nl.ing.lovebird.kafka.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.List;

@ConditionalOnClass(KafkaAdmin.class)
@AutoConfiguration
@EnableConfigurationProperties(KafkaTopicsProperties.class)
@RequiredArgsConstructor
public class KafkaTopicsCreatorAutoConfiguration {

    private final KafkaTopicsProperties kafkaTopicsProperties;

    @Bean
    public KafkaTopicsCreator kafkaTestContainerLauncher(KafkaAdmin kafkaAdmin) {
        return new KafkaTopicsCreator(kafkaTopicsProperties, kafkaAdmin);
    }

    @Slf4j
    @RequiredArgsConstructor
    static class KafkaTopicsCreator implements InitializingBean {
        private final KafkaTopicsProperties kafkaTopicsProperties;
        private final KafkaAdmin kafkaAdmin;

        @Override
        public void afterPropertiesSet() {
            // Creating topics defined in 'yolt.kafka.topics' properties in yaml files
            final List<NewTopic> newTopics = kafkaTopicsProperties.getAsNewTopics();
            if (!newTopics.isEmpty()) {
                try (Admin client = Admin.create(kafkaAdmin.getConfigurationProperties())) {
                    client.createTopics(newTopics);
                }
            }
        }
    }
}
