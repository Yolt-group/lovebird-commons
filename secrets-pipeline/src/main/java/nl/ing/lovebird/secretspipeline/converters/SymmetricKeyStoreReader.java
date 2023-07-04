package nl.ing.lovebird.secretspipeline.converters;

import com.yolt.securityutils.crypto.SecretKey;
import nl.ing.lovebird.secretspipeline.VaultKeys;
import org.bouncycastle.util.encoders.Base64;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static nl.ing.lovebird.secretspipeline.KeyUtils.readFile;

public class SymmetricKeyStoreReader extends KeyStoreReader {

    @Override
    public void read(Path file, VaultKeys keys) {
        byte[] base64EncodedFileContents = readFile(file);
        byte[] decodedFileContents = Base64.decode(base64EncodedFileContents);

        keys.addPrivate(entryName(file), SecretKey.from(decodedFileContents.clone()));
        Arrays.fill(base64EncodedFileContents, (byte) 0);
        Arrays.fill(decodedFileContents, (byte) 0);
    }

    @Override
    public List<String> getKeyExtensions() {
        return Arrays.asList("key_128", "key_160", "key_192", "key_224", "key_256", "key_512");
    }
}
