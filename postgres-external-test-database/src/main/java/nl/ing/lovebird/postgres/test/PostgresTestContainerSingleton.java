package nl.ing.lovebird.postgres.test;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Singleton instance postgres test container.
 * <p>
 * The postgres container is slow to start up. Reusing the same container
 * between tests speeds this up. Note that in Gitlab also provides a single
 * Postgres service. You'll have to ensure that your tests do not reuse
 * identifiers.
 */
@Slf4j
final class PostgresTestContainerSingleton {

    private static PostgreSQLContainer<?> singleton;

    private PostgresTestContainerSingleton() {

    }

    @SuppressWarnings("squid:S1452")
    public static PostgreSQLContainer<?> instance() {
        if (singleton != null && !singleton.isRunning()) {
            singleton.close();
            singleton = null;
        }

        if (singleton == null) {
            singleton = createPostgresTestContainer();
        }
        return singleton;
    }

    private static PostgreSQLContainer<?> createPostgresTestContainer() {
        PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:11.6");
        postgresContainer.withStartupAttempts(1);
        try {
            postgresContainer.start();
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
        Runtime.getRuntime().addShutdownHook(new Thread(postgresContainer::close));
        return postgresContainer;
    }
}
