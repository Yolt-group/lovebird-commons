package nl.lovebird.cassandra.versioning;

import com.datastax.driver.core.Session;
import nl.ing.lovebird.cassandra.test.TestCassandraSession;

import java.nio.file.Paths;

public interface AbstractCassandraTest {
    Session SESSION = TestCassandraSession.provide("foo", Paths.get("src/test/resources/foo-keyspace.cql"));
}
