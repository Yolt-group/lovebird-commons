package nl.ing.lovebird.cassandra.spring.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("cassandra-updates")
public class YoltCassandraUpdatesProperties {

    private int secondsToWait = 60;
}
