package nl.ing.lovebird.cassandra.autoconfigure;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ProtocolOptions;
import com.datastax.driver.core.ProtocolOptions.Compression;
import com.datastax.driver.core.QueryOptions;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Copy of {@link org.springframework.boot.autoconfigure.cassandra.CassandraProperties}.
 * <p>
 * Spring Cassandra currently uses the Cassandra driver v4 while we are still
 * on v3. This enables us to use the spring configuration paths without using
 * Spring Cassandra.
 * <p>
 * <p>
 * Configuration properties for Cassandra.
 */
@Data
@ConfigurationProperties(prefix = "spring.data.cassandra")
public class YoltCassandraProperties {

    /**
     * Cluster node addresses.
     */
    private final List<String> contactPoints = new ArrayList<>(Collections.singleton("localhost"));
    /**
     * Pool configuration.
     */
    private final Pool pool = new Pool();
    /**
     * Keyspace name to use.
     */
    private String keyspaceName;
    /**
     * Name of the Cassandra cluster.
     */
    private String clusterName;
    /**
     * Port of the Cassandra server.
     */
    private int port = ProtocolOptions.DEFAULT_PORT;
    /**
     * Login user of the server.
     */
    private String username;
    /**
     * Login password of the server.
     */
    private String password;
    /**
     * Compression supported by the Cassandra binary protocol.
     */
    private Compression compression = Compression.NONE;
    /**
     * Queries consistency level.
     */
    private ConsistencyLevel consistencyLevel;
    /**
     * Queries serial consistency level.
     */
    private ConsistencyLevel serialConsistencyLevel;
    /**
     * Queries default fetch size.
     */
    private int fetchSize = QueryOptions.DEFAULT_FETCH_SIZE;
    /**
     * Socket option: connection time out.
     */
    private Duration connectTimeout;
    /**
     * Socket option: read time out.
     */
    private Duration readTimeout;
    /**
     * Schema action to take at startup.
     */
    private String schemaAction = "none";
    /**
     * Enable SSL support.
     */
    private boolean ssl = false;
    /**
     * Whether to enable JMX reporting. Default to false as Cassandra JMX reporting is not
     * compatible with Dropwizard Metrics.
     */
    private boolean jmxEnabled;

    /**
     * Pool properties.
     */
    @Data
    public static class Pool {

        /**
         * Idle timeout before an idle connection is removed. If a duration suffix is not
         * specified, seconds will be used.
         */
        @DurationUnit(ChronoUnit.SECONDS)
        private Duration idleTimeout = Duration.ofSeconds(120);

        /**
         * Pool timeout when trying to acquire a connection from a host's pool.
         */
        private Duration poolTimeout = Duration.ofMillis(5000);

        /**
         * Heartbeat interval after which a message is sent on an idle connection to make
         * sure it's still alive. If a duration suffix is not specified, seconds will be
         * used.
         */
        @DurationUnit(ChronoUnit.SECONDS)
        private Duration heartbeatInterval = Duration.ofSeconds(30);

        /**
         * Maximum number of requests that get queued if no connection is available.
         */
        private int maxQueueSize = 256;

    }

}
