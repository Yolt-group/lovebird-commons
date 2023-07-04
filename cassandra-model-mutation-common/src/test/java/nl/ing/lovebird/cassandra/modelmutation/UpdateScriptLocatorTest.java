package nl.ing.lovebird.cassandra.modelmutation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


class UpdateScriptLocatorTest {

    @Test
    void shouldGetPathToUpdateScripts() {
        String updatesFolder = "cassandraUpdates1";

        final List<String> updateScripts = UpdateScriptLocator.getPathsToUpdateScripts(updatesFolder);
        assertThat(fileNamesFromFiles(updateScripts))
                .containsExactly("1-update.cql", "2-update.cql", "3-update.cql");
    }

    @Test
    void shouldGetPathToUpdateScriptsAndShouldSkipBasedOnSnapshots() {
        String updatesFolder = "cassandraUpdates2";

        final List<String> updateScripts = UpdateScriptLocator.getPathsToUpdateScripts(updatesFolder);

        assertThat(fileNamesFromFiles(updateScripts))
                .containsExactly("2-update.snapshot.cql", "3-update.cql");
    }

    @Test
    void shouldThrowExceptionWhenAnInvalidSnapshotIsFound() {
        Assertions.assertThrows(
                IllegalStateException.class,
                () -> UpdateScriptLocator.getPathsToUpdateScripts("cassandraUpdates3"));
    }

    @Test
    void shouldResolveWhenSnapshotIsLastUpdateInList() {
        String updatesFolder = "cassandraUpdates4";

        final List<String> updateScripts = UpdateScriptLocator.getPathsToUpdateScripts(updatesFolder);

        assertThat(fileNamesFromFiles(updateScripts))
                .containsExactly("1-update.snapshot.cql");
    }

    private static List<String> fileNamesFromFiles(List<String> files) {
        return files.stream()
                .map(it -> Paths.get(it).getFileName().toString())
                .collect(Collectors.toList());
    }
}
