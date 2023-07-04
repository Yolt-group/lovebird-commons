package nl.ing.lovebird.kafka.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Data
@ConfigurationProperties("lovebird.kafka")
public class YoltKafkaProperties {

    private Health health;

    @Data
    public static class Health {
        private String topic;
        private Duration timeout;
    }
}
