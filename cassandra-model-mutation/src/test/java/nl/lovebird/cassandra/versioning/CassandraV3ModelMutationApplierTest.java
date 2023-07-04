package nl.lovebird.cassandra.versioning;


import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.utils.UUIDs;
import nl.ing.lovebird.cassandra.modelmutation.CqlFileReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static nl.lovebird.cassandra.versioning.AbstractCassandraModelMutationWaiter.FileExecutionStatus.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CassandraV3ModelMutationApplierTest implements AbstractCassandraTest {

    private CassandraV3ModelMutationWaiter waiter;

    @BeforeEach
    void setUp() {
        waiter = new CassandraV3ModelMutationWaiter(SESSION, 5);
        SESSION.execute("CREATE TABLE IF NOT EXISTS foo.modelmutation(hcpk text, filename text, time timeuuid, script text, result text, user text, forced boolean, PRIMARY KEY (hcpk, filename, time));");
        SESSION.execute("TRUNCATE foo.modelmutation");
    }

    @Test
    void shouldTimeoutIfTheModelMutationTableDoesNotExist() {
        // When
        assertThatThrownBy(() -> waiter.blockUntilApplied(
                CqlFileReader.cqlFiles("cassandraUpdates"),
                Optional.empty()
        )).isInstanceOf(TimeoutException.class);
    }

    @Test
    void shouldTimeoutUnlessAllMutationsAreApplied() throws Exception {

        assertThatThrownBy(() -> waiter.blockUntilApplied(
                CqlFileReader.cqlFiles("cassandraUpdates"),
                Optional.empty())).isInstanceOf(TimeoutException.class);

        insertModelMutation("1-foo.cql", OK);

        assertThatThrownBy(() -> waiter.blockUntilApplied(
                CqlFileReader.cqlFiles("cassandraUpdates"),
                Optional.empty()
        )).isInstanceOf(TimeoutException.class);

        insertModelMutation("2-bar.cql", SKIP);

        assertThatThrownBy(() -> waiter.blockUntilApplied(
                CqlFileReader.cqlFiles("cassandraUpdates"),
                Optional.empty()
        )).isInstanceOf(TimeoutException.class);

        insertModelMutation("3-foo.cql", NOT_EXECUTED);

        assertThatThrownBy(() -> waiter.blockUntilApplied(
                CqlFileReader.cqlFiles("cassandraUpdates"),
                Optional.empty()
        )).isInstanceOf(TimeoutException.class);

        insertModelMutation("3-foo.cql", UNKNOWN);

        assertThatThrownBy(() -> waiter.blockUntilApplied(
                CqlFileReader.cqlFiles("cassandraUpdates"),
                Optional.empty()
        )).isInstanceOf(TimeoutException.class);

        insertModelMutation("3-foo.cql", "invalid-file-execution-status");

        assertThatThrownBy(() -> waiter.blockUntilApplied(
                CqlFileReader.cqlFiles("cassandraUpdates"),
                Optional.empty()
        )).isInstanceOf(TimeoutException.class);

        insertModelMutation("3-foo.cql", OK);

        assertThatThrownBy(() -> waiter.blockUntilApplied(
                CqlFileReader.cqlFiles("cassandraUpdates"),
                Optional.empty()
        )).isInstanceOf(TimeoutException.class);

        insertModelMutation("4-foo bar.cql", OK);

        waiter.blockUntilApplied(
                CqlFileReader.cqlFiles("cassandraUpdates"),
                Optional.empty()
        );
    }

    @Test
    void testDifferentKeyspace() {
        assertThatThrownBy(() -> waiter.blockUntilApplied(
                CqlFileReader.cqlFiles("cassandraUpdates"),
                Optional.of("not_foo")
        )).isInstanceOf(ExecutionException.class).hasRootCauseInstanceOf(InvalidQueryException.class);
    }

    private void insertModelMutation(String fileName, CassandraV3ModelMutationWaiter.FileExecutionStatus fileExecutionStatus) {
        insertModelMutation(fileName, fileExecutionStatus.toString());
    }

    private void insertModelMutation(String fileName, String fileExecutionStatusString) {
        SESSION.execute(QueryBuilder.insertInto("foo", "modelmutation")
                .value("hcpk", "HCPK")
                .value("filename", fileName)
                .value("time", UUIDs.timeBased())
                .value("forced", false)
                .value("result", fileExecutionStatusString));
    }
}
