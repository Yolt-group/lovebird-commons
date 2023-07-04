package nl.ing.lovebird.cassandra.spring.versioning;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import nl.ing.lovebird.cassandra.test.TestCassandraCluster;
import nl.ing.lovebird.cassandra.spring.versioning.CassandraV4ModelMutationWaiter.FileExecutionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;
import static nl.ing.lovebird.cassandra.spring.versioning.CassandraV4ModelMutationWaiter.FileExecutionStatus.NOT_EXECUTED;
import static nl.ing.lovebird.cassandra.spring.versioning.CassandraV4ModelMutationWaiter.FileExecutionStatus.OK;
import static nl.ing.lovebird.cassandra.spring.versioning.CassandraV4ModelMutationWaiter.FileExecutionStatus.SKIP;
import static nl.ing.lovebird.cassandra.spring.versioning.CassandraV4ModelMutationWaiter.FileExecutionStatus.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CassandraV4ModelMutationWaiterTest {

    private CassandraV4ModelMutationWaiter waiter;

    @BeforeEach
    void beforeAll() {
        TestCassandraCluster.provideCluster();
    }

    @BeforeEach
    void beforeEach() {
        CqlSessionBuilder cqlSessionBuilder = createSessionBuilder("foo");

        waiter = new CassandraV4ModelMutationWaiter(3, cqlSessionBuilder, "cassandraUpdates");

        execute(createSessionBuilder(null), "CREATE KEYSPACE IF NOT EXISTS foo WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 1};");
        execute(createSessionBuilder("foo"), "CREATE TABLE IF NOT EXISTS modelmutation(hcpk text, filename text, time timeuuid, script text, result text, user text, forced boolean, PRIMARY KEY (hcpk, filename, time));");
        execute(createSessionBuilder("foo"), "TRUNCATE modelmutation");
    }

    @Test
    void shouldThrowExceptionIfCassandraUpdatesDirectoryIsMissing() {

        waiter = new CassandraV4ModelMutationWaiter(3, createSessionBuilder("foo"), "/src/main/resources/missing");

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

    private void insertModelMutation(String fileName, FileExecutionStatus fileExecutionStatus) {
        String query = QueryBuilder.insertInto("modelmutation")
                .value("hcpk", literal("HCPK"))
                .value("filename", literal(fileName))
                .value("time", literal(Uuids.timeBased()))
                .value("forced", literal(false))
                .value("result", literal(fileExecutionStatus.toString())).asCql();

        execute(createSessionBuilder("foo"), query);
    }

    private static void execute(CqlSessionBuilder cqlSessionBuilder, String query) {
        try (CqlSession session = cqlSessionBuilder.build()) {
            session.execute(query);
        }
    }

    private static CqlSessionBuilder createSessionBuilder(String keyspace) {
            return new CqlSessionBuilder()
                .addContactPoint(new InetSocketAddress(TestCassandraCluster.getClusterHost(), TestCassandraCluster.getClusterPort()))
                .withLocalDatacenter("datacenter1")
                .withKeyspace(keyspace);
    }
}