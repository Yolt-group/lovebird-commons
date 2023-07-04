package nl.ing.lovebird.cassandra.modelmutation;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;

@Slf4j
public class UpdateScriptLocator {

    private UpdateScriptLocator() {
        // Prevent construction since this is a utils class
    }

    public static List<String> getPathsToUpdateScripts() {
        return getPathsToUpdateScripts("cassandraUpdates");
    }

    static List<String> getPathsToUpdateScripts(String cassandraUpdatesFolder) {

        try {
            // Get directory cassandraUpdates
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            URI updatesUri = classLoader.getResource(cassandraUpdatesFolder).toURI();

            // Use Java NIO to join folders, this makes it platform independent
            Path updatesPath = Paths.get(updatesUri);

            // Check for a <cassandraUpdatesFolder>Snapshot folder
            URL updatesSnapshotResource = classLoader.getResource(cassandraUpdatesFolder + "Snapshot");
            Optional<Path> snapshot = Optional.empty();
            if (updatesSnapshotResource != null) {
                Path snapshotFolder = Paths.get(updatesSnapshotResource.toURI());

                if (snapshotFolder.toFile().isDirectory()) {
                    try (Stream<Path> snapshots = Files.list(snapshotFolder)) {

                        // Only take the last snapshot
                        snapshot = snapshots
                                .filter(p -> p.toString().endsWith(".snapshot.cql"))
                                .max(naturalOrder());
                    }
                }
            }

            try (Stream<Path> stream = Files.list(updatesPath)) {

                List<String> allUpdateFiles = stream
                        .filter(f -> !f.toFile().isDirectory())
                        .map(Object::toString)
                        .sorted()
                        .collect(Collectors.toCollection(LinkedList::new));

                if (snapshot.isPresent()) {
                    Path snapshotFile = snapshot.get();
                    String snapshotOfUpdateFile = snapshotFile.getFileName().toString().replace(".snapshot", "");
                    String snapshotOfUpdatePath = Paths.get(cassandraUpdatesFolder, snapshotOfUpdateFile).toString();

                    Optional<String> optionalSnapshot = allUpdateFiles.stream()
                            .filter(it -> it.endsWith(snapshotOfUpdatePath))
                            .findAny();

                    if (optionalSnapshot.isPresent()) {
                        // concatenate the snapshot file and the remaining update scripts that came after the snapshot
                        int snapshotIndex = allUpdateFiles.indexOf(optionalSnapshot.get());
                        allUpdateFiles = allUpdateFiles.subList(snapshotIndex + 1, allUpdateFiles.size());
                        allUpdateFiles.add(0, Objects.toString(snapshotFile));
                    } else {
                        throw new IllegalStateException("Found a snapshot in src/main/resources/" + cassandraUpdatesFolder + "Snapshot, but it is not based on an actual cassandra update script file. " +
                                "The name of the snapshot file should be equal to the last update file where it's based on.");
                    }
                }

                return allUpdateFiles;
            }

        } catch (IllegalStateException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new IllegalStateException("src/main/resources/cassandraUpdates should exist and contain versioning scripts!", e);
        }
    }
}
