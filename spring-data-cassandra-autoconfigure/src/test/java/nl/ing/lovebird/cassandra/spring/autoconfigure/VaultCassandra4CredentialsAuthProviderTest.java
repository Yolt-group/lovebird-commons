package nl.ing.lovebird.cassandra.spring.autoconfigure;

import com.datastax.oss.driver.api.core.auth.PlainTextAuthProviderBase;
import com.datastax.oss.driver.api.core.metadata.EndPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class VaultCassandra4CredentialsAuthProviderTest {

    @Test
    @DisplayName("[SHOULD] create authenticator [GIVEN] given existing secrets properties file")
    void shouldCreateAuthenticator_givenExistingSecretsPropertiesFile(@TempDir Path tempDir) throws Exception {
        Path secretsFile = tempDir.resolve("secrets");
        Properties properties = new Properties();
        properties.setProperty("username", "test_username");
        properties.setProperty("password", "test_password");

        try (final OutputStream outputstream = new FileOutputStream(secretsFile.toFile())) {
            properties.store(outputstream, "credentials stored");
        }

        YoltCassandra4VaultCredentialsAuthProvider vaultCassandraCredentialsAuthProvider = new YoltCassandra4VaultCredentialsAuthProvider(secretsFile);
        assertThatCode(() -> vaultCassandraCredentialsAuthProvider.newAuthenticator(mock(EndPoint.class), null))
                        .doesNotThrowAnyException();

        PlainTextAuthProviderBase.Credentials credentials = vaultCassandraCredentialsAuthProvider.getCredentials(null, null);
        assertThat(credentials.getUsername()).containsExactly("test_username".toCharArray());
        assertThat(credentials.getPassword()).containsExactly("test_password".toCharArray());
    }

    @Test
    @DisplayName("[SHOULD] throw exception [GIVEN] given non-existent secrets properties file")
    void shouldThrownException_givenNonExistantSecretsPropertiesFile() {
        String nonExistentFile = "/dummy/path/secrets.properties";
        YoltCassandra4VaultCredentialsAuthProvider vaultCassandraCredentialsAuthProvider = new YoltCassandra4VaultCredentialsAuthProvider(Paths.get(nonExistentFile));

        assertThatThrownBy(() -> vaultCassandraCredentialsAuthProvider.newAuthenticator(mock(EndPoint.class), "serverAuthenticator"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unable to load secrets from file:" + nonExistentFile);
    }
}
