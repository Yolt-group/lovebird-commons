package nl.ing.lovebird.vault;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

@Slf4j
@UtilityClass
public class Vault {

    public void requireFileProvidedByVault(Path file) {
        if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
            return;
        }
        throw createVaultDidNotProvideFileException(file);
    }

    private static IllegalArgumentException createVaultDidNotProvideFileException(Path file) {
        return new IllegalArgumentException("" +
                "The file '" + file + "' was not provided in time by vault-agent\n" +
                "Please check if vault-agent is running, correctly configured and able to provide the file.\n" +
                "You can check the vault agent logs by using `kubectl logs <pod> -c vault-agent`.");
    }
}
