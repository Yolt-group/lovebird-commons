package nl.ing.lovebird.cassandra.test;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.CassandraContainer;
import org.testcontainers.containers.ContainerLaunchException;

/**
 * Singleton instance cassandra test container.
 * <p>
 * The cassandra container is slow to start up. Reusing the same container
 * between tests speeds this up. Note that in Gitlab also provides a single
 * Cassandra service. You'll have to ensure that your tests do not reuse
 * identifiers.
 */
@Slf4j
final class CassandraTestContainerSingleton {

    private static CassandraContainer<?> singleton;

    private CassandraTestContainerSingleton() {

    }

    @SuppressWarnings("squid:S1452")
    public static CassandraContainer<?> instance() {
        if (singleton != null && !singleton.isRunning()) {
            singleton.close();
            singleton = null;
        }

        if (singleton == null) {
            singleton = createCassandraTestContainer();
        }
        return singleton;
    }

    private static CassandraContainer<?> createCassandraTestContainer() {
        CassandraContainer<?> cassandraContainer = new CassandraContainer<>("cassandra:3.11.2");
        cassandraContainer.withStartupAttempts(1);
        try {
            cassandraContainer.start();
        } catch (ContainerLaunchException e) {
            // Let's make sure it's easy to find that we need a running Docker
            String errorMessage = "\n" +
                    " ___________________________________________\n" +
                    "/ Couldn't find a valid Docker environment. \\\n" +
                    "\\ Did you start the Docker deamon?          /\n" +
                    " -------------------------------------------\n" +
                    "        \\   ^__^\n" +
                    "         \\  (xx)\\_______\n" +
                    "            (__)\\       )\\/\\\n" +
                    "             U  ||----w |\n" +
                    "                ||     ||";
            log.error(errorMessage, e);
            throw e;
        }
        Runtime.getRuntime().addShutdownHook(new Thread(cassandraContainer::close));
        return cassandraContainer;
    }
}
