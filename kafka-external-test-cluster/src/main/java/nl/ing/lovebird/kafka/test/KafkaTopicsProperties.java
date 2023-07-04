package nl.ing.lovebird.kafka.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@ConfigurationProperties(prefix = "yolt.kafka")
@Slf4j
public class KafkaTopicsProperties {

    private static final int DEFAULT_PARTITIONS = 1;

    private Map<String, TopicEntry> topics;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopicEntry {
        String topicName;
        Integer listenerConcurrency;
    }

    public List<NewTopic> getAsNewTopics() {
        if (topics == null || topics.isEmpty()) {
            log.warn("Topics for creation in test context are not defined, you should consider define those " +
                    "in 'yolt.kafka.topics' in case they are required for integration tests");
            return Collections.emptyList();
        }

        return topics.values().stream()
                .filter(Objects::nonNull)
                .map(topicEntry -> new NewTopic(topicEntry.getTopicName(), topicEntry.getListenerConcurrency() == null ? DEFAULT_PARTITIONS : topicEntry.getListenerConcurrency(), (short) 1))
                .collect(Collectors.toList());
    }
}
