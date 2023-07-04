package nl.ing.lovebird.secretspipeline.converters;

import nl.ing.lovebird.secretspipeline.VaultKeys;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static nl.ing.lovebird.secretspipeline.KeyUtils.getKeyType;

@Slf4j
public abstract class KeyStoreReader {

    public static final String TYPE_INDICATOR = "type: ";

    abstract List<String> getKeyExtensions();

    public abstract void read(Path file, VaultKeys keys) throws Exception;

    public final boolean isApplicable(Path file) {
        Optional<String> entryName = getKeyType(file);
        return entryName.map(e -> getKeyExtensions().stream().anyMatch(e::equalsIgnoreCase)).orElse(false);
    }

    final String entryName(Path file) {
        return file.getFileName().toString();
    }
}
