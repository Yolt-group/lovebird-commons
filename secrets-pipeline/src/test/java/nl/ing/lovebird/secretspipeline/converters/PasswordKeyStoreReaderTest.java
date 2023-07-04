package nl.ing.lovebird.secretspipeline.converters;

import nl.ing.lovebird.secretspipeline.VaultKeys;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static nl.ing.lovebird.secretspipeline.converters.TestUtil.createSymmetricKeyFile;
import static java.nio.charset.StandardCharsets.UTF_8;

class PasswordKeyStoreReaderTest {

    private VaultKeys vaultKeys = new VaultKeys();

    @Test
    void readPasswordKeyFromFile(@TempDir Path tempDir) throws Exception {
        String password = "this_is_a_password";
        Path passwordFile = createSymmetricKeyFile(password.getBytes(UTF_8), tempDir, "test", "password_alfa_numeric", true);

        new PasswordKeyStoreReader().read(passwordFile, vaultKeys);

        Assertions.assertThat(vaultKeys.getPassword("test").getEncoded()).isEqualTo(password.getBytes(UTF_8));
    }

    @Test
    void differentKeyTypeShouldNotBePickedUp(@TempDir Path tempDir) throws Exception {
        String password = "this_is_a_password";
        Path passwordFile = createSymmetricKeyFile(password.getBytes(UTF_8), tempDir, "test", "test", true);

        Assertions.assertThat(new PasswordKeyStoreReader().isApplicable(passwordFile)).isFalse();
    }
}
