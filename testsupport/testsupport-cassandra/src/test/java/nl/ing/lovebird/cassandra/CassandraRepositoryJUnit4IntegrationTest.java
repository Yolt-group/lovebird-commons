package nl.ing.lovebird.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.annotations.Table;
import nl.ing.lovebird.testsupport.cassandra.AbstractCassandraTest;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * We're testing `nl.ing.lovebird:cassandra` here to avoid a circular
 * dependency on `nl.ing.lovebird:testsupport-cassandra`
 *
 * Note: This is a JUnit 4 test. Some refactoring is need to make the
 * environment variables stubbable on Java 11+.
 */
public class CassandraRepositoryJUnit4IntegrationTest implements AbstractCassandraTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private Session sessionUnboundToKeyspace;

    @Before
    public void setup() {
        Session session = Mockito.spy(MY_KEYSPACE_SESSION);
        this.sessionUnboundToKeyspace = MY_KEYSPACE_SESSION.getCluster().connect();

        Table table = User.class.getAnnotation(Table.class);

        String cql = String.format("TRUNCATE TABLE %s", table.name());
        session.execute(cql);

        Mockito.reset(session);
    }

    @Test
    @Ignore("Does not work on Java 11+")
    public void testKeyspaceEnvironmentalVariable() {
        new UserRepository(sessionUnboundToKeyspace, "my_keyspace"); // should be ok (default)

        environmentVariables.set(CassandraRepository.MY_POD_NAMESPACE_ENV_VARIABLE, CassandraRepository.MY_POD_NAMESPACE_DEFAULT_NAMESPACE);
        new UserRepository(sessionUnboundToKeyspace, "my_keyspace"); // should be ok (default)

        environmentVariables.set(CassandraRepository.MY_POD_NAMESPACE_ENV_VARIABLE, "ycs");// should be ok (this is how it should be configured)
        new UserRepository(sessionUnboundToKeyspace, "ycs_test");

        new ColumnRepository(sessionUnboundToKeyspace); // should be ok (system keyspace)

        try {
            new UserRepository(sessionUnboundToKeyspace, "my_keyspace");
            fail("should have failed");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo("Unable to create CassandraRepository. Keyspace should be prefixed with ycs");
        }
    }

}
