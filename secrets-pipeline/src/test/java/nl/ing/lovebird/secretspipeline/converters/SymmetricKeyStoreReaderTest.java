package nl.ing.lovebird.secretspipeline.converters;

import com.yolt.securityutils.crypto.CryptoUtils;
import com.yolt.securityutils.crypto.util.Hex;
import nl.ing.lovebird.secretspipeline.VaultKeys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static nl.ing.lovebird.secretspipeline.converters.TestUtil.createSymmetricKeyFile;
import static org.assertj.core.api.Assertions.assertThat;

class SymmetricKeyStoreReaderTest {

    private final VaultKeys vaultKeys = new VaultKeys();

    @Test
    void _256(@TempDir Path tempDir) throws Exception {
        byte[] key = CryptoUtils.getRandomKey();
        Path file = createSymmetricKeyFile(key, tempDir, "test", "key_256", true);

        new SymmetricKeyStoreReader().read(file, vaultKeys);

        assertThat(vaultKeys.getSymmetricKey("test").toHex()).isEqualTo(Hex.toHex(key));
    }

    @Test
    void _512(@TempDir Path tempDir) throws Exception {
        byte[] key = CryptoUtils.getRandomKey(512);
        Path file = createSymmetricKeyFile(key, tempDir, "test", "key_512", true);

        new SymmetricKeyStoreReader().read(file, vaultKeys);

        assertThat(vaultKeys.getSymmetricKey("test").toHex()).isEqualTo(Hex.toHex(key));
    }

    @Test
    void fromPipeline(@TempDir Path tempDir) throws Exception {
        String actualPipelineInjectionResult = "wyO3kfkdUXWRsmLUs/0oTA=="; //isolated from an actual pipeline injection run
        Path file = createSymmetricKeyFile(actualPipelineInjectionResult.getBytes(), tempDir, "test", "key_128", false);

        new SymmetricKeyStoreReader().read(file, vaultKeys);

        assertThat(vaultKeys.getSymmetricKey("test").toHex()).isEqualTo(Hex.fromBase64(actualPipelineInjectionResult));
    }
}

