package nl.ing.lovebird.kafka.test;

import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Singleton instance kafka test container.
 * <p>
 * The kafka container is slow to start up. Reusing the same container
 * between tests speeds this up. Note that in Gitlab also provides a single
 * kafka service. You'll have to ensure that your tests do not reuse
 * identifiers.
 */
@Slf4j
final class KafkaTestContainerSingleton {

    private static KafkaContainer singleton;

    private KafkaTestContainerSingleton() {

    }

    @SuppressWarnings("squid:S1452")
    public static KafkaContainer instance() {
        if (singleton != null && !singleton.isRunning()) {
            singleton.close();
            singleton = null;
        }

        if (singleton == null) {
            singleton = createTestContainer();
        }
        return singleton;
    }

    private static KafkaContainer createTestContainer() {
        DockerImageName image = DockerImageName.parse("confluentinc/cp-kafka:5.4.3");
        KafkaContainer kafkaContainer = new KafkaContainer(image);
        try {
            kafkaContainer.start();
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
        Runtime.getRuntime().addShutdownHook(new Thread(kafkaContainer::close));
        return kafkaContainer;
    }
}
