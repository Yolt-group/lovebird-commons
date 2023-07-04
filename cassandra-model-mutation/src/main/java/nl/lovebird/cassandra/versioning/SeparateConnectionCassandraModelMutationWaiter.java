package nl.lovebird.cassandra.versioning;

import com.datastax.driver.core.Cluster;
import nl.ing.lovebird.cassandra.modelmutation.CqlFileReader;

import java.util.Optional;

/**
 * This version of the applier is only used by the backend service. Data science uses the
 * {@link CassandraV3ModelMutationWaiter} which relies on an existing session instead of setting up a separate connection.
 */
public class SeparateConnectionCassandraModelMutationWaiter extends AbstractCassandraModelMutationWaiter {

    private final Cluster.Builder clusterInitializer;
    private final String keySpaceName;
    private final String cqlDirectory;

    public SeparateConnectionCassandraModelMutationWaiter(final String cqlDirectory,
                                                          final String keySpaceName,
                                                          final Cluster.Builder clusterInitializer,
                                                          final int secondsToWait) {
        super(secondsToWait);
        this.clusterInitializer = clusterInitializer;
        this.keySpaceName = keySpaceName;
        this.cqlDirectory = cqlDirectory;
    }

    public void blockUntilApplied() {
        try (CassandraConnector cassandraConnector = new CassandraConnector()) {

            cassandraConnector.connect(clusterInitializer, keySpaceName);

            blockUntilApplied(CqlFileReader.cqlFiles(cqlDirectory),
                    Optional.of(keySpaceName),
                    cassandraConnector.getSession());
        } catch (Exception e) {
            throw new RuntimeException("Error while waiting for CQL files to be applied", e);
        }
    }

}
