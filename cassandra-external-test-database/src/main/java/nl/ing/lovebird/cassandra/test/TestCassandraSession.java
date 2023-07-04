package nl.ing.lovebird.cassandra.test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Creates a Cassandra session for testing.
 * <p>
 * Note: For use in lovebird-commons only. When testing spring application
 * please use {@link EnableExternalCassandraTestDatabase}.
 */
@UtilityClass
public class TestCassandraSession {

    public Session provide(final String keyspace, final Path... initScripts) {
        final Cluster cluster = TestCassandraCluster.provideCluster();

        if (initScripts != null && initScripts.length > 0) {
            final List<String> statements = splitToStatements(initScripts);
            final Session initScriptsSession = cluster.connect();
            statements.forEach(initScriptsSession::execute);
            initScriptsSession.close();
        }

        return cluster.connect(keyspace);
    }

    private List<String> splitToStatements(final Path... initScripts) {
        return Arrays.stream(initScripts)
                .map(path -> {
                    try {
                        return String.join(" ", Files.readAllLines(path, StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to read file " + path, e);
                    }
                })
                .map(file -> file.split(";"))
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .filter(file -> !file.trim().isEmpty())
                .collect(Collectors.toList());
    }
}
