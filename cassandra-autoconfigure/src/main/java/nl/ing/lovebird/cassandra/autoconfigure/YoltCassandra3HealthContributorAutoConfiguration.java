package nl.ing.lovebird.cassandra.autoconfigure;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Map;

/**
 * {@link org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration} does not instantiate
 * the LoadBalancingPolicies or ReconnectionPolicies or RetryPolicies correctly since its trying to create an instance
 * from the NoArgs constructor which is private or does not exists.
 *
 * @author Algin Maduro
 */
@ConditionalOnClass({Cluster.class, HealthContributor.class})
@ConditionalOnEnabledHealthIndicator("cassandra")
@AutoConfiguration(after = YoltCassandra3AutoConfiguration.class)
@EnableConfigurationProperties(YoltCassandraProperties.class)
@Slf4j
public class YoltCassandra3HealthContributorAutoConfiguration {

    private final YoltCassandraProperties cassandraProperties;


    public YoltCassandra3HealthContributorAutoConfiguration(YoltCassandraProperties cassandraProperties) {
        this.cassandraProperties = cassandraProperties;

        if (cassandraProperties.getKeyspaceName() == null || cassandraProperties.getKeyspaceName().isEmpty()) {
            throw new IllegalArgumentException("Please set the keyspace name for the keyspace you're trying to connect to, via the property 'spring.data.cassandra.keyspace-name'");
        }
    }

    @Bean
    @ConditionalOnBean(Session.class)
    public HealthContributor cassandraHealthIndicator(Map<String, Session> cassandraSessions) {
        if (cassandraSessions.size() == 1) {
            return new YoltCassandra3HealthContributor(cassandraProperties.getKeyspaceName(), cassandraSessions.values().iterator().next());
        }
        return CompositeHealthContributor.fromMap(cassandraSessions, session -> new YoltCassandra3HealthContributor(cassandraProperties.getKeyspaceName(), session));
    }

}
