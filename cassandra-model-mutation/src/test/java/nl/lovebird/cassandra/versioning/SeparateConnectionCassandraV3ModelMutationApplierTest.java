package nl.lovebird.cassandra.versioning;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.EndPoint;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.utils.UUIDs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static nl.lovebird.cassandra.versioning.AbstractCassandraModelMutationWaiter.FileExecutionStatus.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SeparateConnectionCassandraV3ModelMutationApplierTest implements AbstractCassandraTest {

    private Cluster.Builder clusterInitializer;
    private SeparateConnectionCassandraModelMutationWaiter waiter;

    @BeforeEach
    public void setUp() {
        clusterInitializer = clusterInitializer(SESSION.getCluster());
        waiter = new SeparateConnectionCassandraModelMutationWaiter("cassandraUpdates", "foo", clusterInitializer, 3);
        SESSION.execute("CREATE TABLE IF NOT EXISTS foo.modelmutation(hcpk text, filename text, time timeuuid, script text, result text, user text, forced boolean, PRIMARY KEY (hcpk, filename, time));");
        SESSION.execute("TRUNCATE foo.modelmutation");
    }

    @Test
    void shouldThrowExceptionIfCassandraUpdatesDirectoryIsMissing() {

        waiter = new SeparateConnectionCassandraModelMutationWaiter("/src/main/resources/missing", "foo", clusterInitializer, 3);

        // When
        assertThatThrownBy(() -> waiter.blockUntilApplied()).hasCauseInstanceOf(FileNotFoundException.class);
    }

    @Test
    void shouldTimeoutIfTheModelMutationTableDoesNotExist() {
        // When
        assertThatThrownBy(() -> waiter.blockUntilApplied()).hasCauseInstanceOf(TimeoutException.class);
    }

    @Test
    void shouldTimeoutUnlessAllMutationsAreApplied() {

        assertThatThrownBy(() -> waiter.blockUntilApplied()).hasCauseInstanceOf(TimeoutException.class);

        insertModelMutation("1-foo.cql", OK);

        assertThatThrownBy(() -> waiter.blockUntilApplied()).hasCauseInstanceOf(TimeoutException.class);

        insertModelMutation("2-bar.cql", SKIP);

        assertThatThrownBy(() -> waiter.blockUntilApplied()).hasCauseInstanceOf(TimeoutException.class);

        insertModelMutation("3-foo.cql", NOT_EXECUTED);

        assertThatThrownBy(() -> waiter.blockUntilApplied()).hasCauseInstanceOf(TimeoutException.class);

        insertModelMutation("3-foo.cql", UNKNOWN);

        assertThatThrownBy(() -> waiter.blockUntilApplied()).hasCauseInstanceOf(TimeoutException.class);

        insertModelMutation("3-foo.cql", OK);

        assertThatThrownBy(() -> waiter.blockUntilApplied()).hasCauseInstanceOf(TimeoutException.class);

        insertModelMutation("4-foo bar.cql", OK);

        assertDoesNotThrow(() -> waiter.blockUntilApplied());
    }

    private void insertModelMutation(String fileName, AbstractCassandraModelMutationWaiter.FileExecutionStatus fileExecutionStatus) {
        SESSION.execute(QueryBuilder.insertInto("foo", "modelmutation")
                .value("hcpk", "HCPK")
                .value("filename", fileName)
                .value("time", UUIDs.timeBased())
                .value("forced", false)
                .value("result", fileExecutionStatus.toString()));
    }

    public Cluster.Builder clusterInitializer(Cluster cluster) {
        Object manager = ReflectionTestUtils.getField(cluster, "manager");
        List<EndPoint> contactpoints = (List<EndPoint>) ReflectionTestUtils.getField(manager, "contactPoints");
        InetSocketAddress firstContactPoint = contactpoints.get(0).resolve();
        return Cluster.builder()
                .addContactPoints(firstContactPoint.getAddress().getHostAddress())
                .withPort(firstContactPoint.getPort());
    }
}
