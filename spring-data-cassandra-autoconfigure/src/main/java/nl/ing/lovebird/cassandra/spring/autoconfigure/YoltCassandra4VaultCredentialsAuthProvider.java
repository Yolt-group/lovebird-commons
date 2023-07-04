package nl.ing.lovebird.cassandra.spring.autoconfigure;

import com.datastax.oss.driver.api.core.auth.PlainTextAuthProviderBase;
import com.datastax.oss.driver.api.core.metadata.EndPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

@Slf4j
public class YoltCassandra4VaultCredentialsAuthProvider extends PlainTextAuthProviderBase {
    private final Path vaultCredentialFile;
    private String username;

    protected YoltCassandra4VaultCredentialsAuthProvider(Path vaultCredentialFile) {
        super("");
        this.vaultCredentialFile = vaultCredentialFile;
    }

    private static Properties readCredentials(Path credentialsFilePath) {
        try (BufferedReader fileReader = Files.newBufferedReader(credentialsFilePath)) {
            Properties properties = new Properties();
            properties.load(fileReader);
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load secrets from file:" + credentialsFilePath);
        }
    }

    @NonNull
    @Override
    public Credentials getCredentials(@NonNull EndPoint endPoint, @NonNull String serverAuthenticator) {
        final Properties credentials = readCredentials(vaultCredentialFile);
        String newUsername = (String) credentials.get("username");
        if (!Objects.equals(username, newUsername)) {
            log.info("Vault-injector provided a new username");
            username = newUsername;
        }
        String password = (String) credentials.get("password");
        return new Credentials(username.toCharArray(), password.toCharArray());
    }
}
