package nl.ing.lovebird.testsupport.cassandra;

import com.datastax.driver.core.Session;
import nl.ing.lovebird.cassandra.test.TestCassandraSession;

import java.nio.file.Paths;

public interface AbstractCassandraTest {
    String MY_KEYSPACE = "my_keyspace";
    String TEST_KEYSPACE = "test_keyspace";
    Session MY_KEYSPACE_SESSION = TestCassandraSession.provide(MY_KEYSPACE,
            Paths.get("src/test/resources/init-keyspaces.cql"),
            Paths.get("src/test/resources/cassandra-schema.cql"),
            Paths.get("src/test/resources/testsupport-schema.cql"));
    Session TEST_KEYSPACE_SESSION = MY_KEYSPACE_SESSION.getCluster().connect(TEST_KEYSPACE);
}
