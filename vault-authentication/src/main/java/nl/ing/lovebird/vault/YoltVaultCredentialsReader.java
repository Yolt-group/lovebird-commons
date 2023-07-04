package nl.ing.lovebird.vault;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YoltVaultCredentialsReader {

    public static Properties readCredentials(Path credentialsFilePath) {
        try (BufferedReader fileReader = Files.newBufferedReader(credentialsFilePath)) {
            Properties properties = new Properties();
            properties.load(fileReader);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load secrets from file:" + credentialsFilePath);
        }
    }
}
