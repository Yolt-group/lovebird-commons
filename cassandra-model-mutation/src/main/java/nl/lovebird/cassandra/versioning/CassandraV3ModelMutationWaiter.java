package nl.lovebird.cassandra.versioning;

import com.datastax.driver.core.Session;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

/**
 * This version of the CassandraV3ModelMutationWaiter is only used by data science. The backend services use the
 * SeparateConnectionCassandraModelMutationApplier which sets up it's own connection instead of relying on an existing
 * session.
 */
@Slf4j
public class CassandraV3ModelMutationWaiter extends AbstractCassandraModelMutationWaiter {

    final Session session;

    /**
     * @deprecated Datascience uses this version of the cassandra applier. They pass in a list of cql
     * files, so it doesn't make any sense to pass in the cqlDirectory here.
     * Use the other constructor instead.
     */
    @Deprecated
    public CassandraV3ModelMutationWaiter(final String cqlDirectory,
                                          final Session session,
                                          final int secondsToWait) {
        super(secondsToWait);
        this.session = session;
    }

    public CassandraV3ModelMutationWaiter(final Session session,
                                          final int secondsToWait) {
        super(secondsToWait);
        this.session = session;
    }

    public void blockUntilApplied(List<String> cqlFiles, final Optional<String> keyspace) {
        blockUntilApplied(cqlFiles, keyspace, session);
    }

}
