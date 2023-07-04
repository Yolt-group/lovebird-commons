package nl.ing.lovebird.cassandra.autoconfigure;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

@RequiredArgsConstructor
class YoltCassandra3HealthContributor extends AbstractHealthIndicator {
    private final String keyspaceName;
    private final Session session;

    /**
     * Query Cassandra to access the model mutation table, and if that succeeds mark Cassandra as up;
     * If that fails exception thrown will mark Cassandra as down.
     * <p>
     * At the time this class was made (spring-boot 1.4) spring-data did not support the newer 3.x branch of Cassandra
     * yet. Spring Data was dropped and a copy of the health indicator included here. Should we move back to Spring Data
     * Cassandra then we can drop this class.
     * <p>
     * Recently we experienced an issue where the service couldn't access keyspaces due to lack of permissions.
     * This didn't set the Cassandra connection as unhealthy because the service could still access the
     * resource (system.local) we use for the health check. So instead we use the key space name used by the pod and
     * check for the well known model mutation table.
     */
    @Override
    protected void doHealthCheck(Health.Builder builder) {
        Select select = QueryBuilder.select("hcpk")
                .from(keyspaceName, "modelmutation")
                // Does not exist. Makes query efficient.
                .where(QueryBuilder.eq("hcpk", "cassandra-health-indicator-check"))
                .limit(1);

        select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        ResultSet results = session.execute(select);

        // Will throw if table was not accessible
        results.all();

        builder.up().withDetail("modelmutation", "accessible");
    }
}
