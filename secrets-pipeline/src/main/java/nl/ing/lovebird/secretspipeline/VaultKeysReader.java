package nl.ing.lovebird.secretspipeline;

import nl.ing.lovebird.secretspipeline.converters.KeyStoreReader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class VaultKeysReader {

    private List<KeyStoreReader> keyConverters;

    public VaultKeysReader(List<KeyStoreReader> keyConverters) {
        this.keyConverters = keyConverters;
    }

    public VaultKeys readFiles(URI secretsLocation) throws IOException {
        VaultKeys vaultKeys = new VaultKeys();
        try (Stream<Path> paths = Files.walk(Paths.get(secretsLocation))) {
            paths.filter(Files::isRegularFile)
                    .forEach(file -> readFile(file, keyConverters, vaultKeys));
        }
        return vaultKeys;
    }

    private void readFile(final Path file, List<KeyStoreReader> keyConverters, VaultKeys vaultKeys) {
        if (!Files.exists(file) || fileSize(file) <= 0L) {
            log.info("Skipping: {}", file);
            return;
        }

        for (KeyStoreReader keyConverter : keyConverters) {
            if (keyConverter.isApplicable(file)) {
                try {
                    keyConverter.read(file, vaultKeys);
                } catch (Exception e) {
                    log.error("Unable to load from file {}, key will not be added and skipped", file, e);
                }
            }
        }
    }

    private long fileSize(Path file) {
        try {
            return Files.size(file);
        } catch (IOException e) {
            return 0L;
        }
    }
}