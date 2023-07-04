package nl.ing.lovebird.secretspipeline.converters;

import com.yolt.securityutils.crypto.PasswordKey;
import nl.ing.lovebird.secretspipeline.VaultKeys;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static nl.ing.lovebird.secretspipeline.KeyUtils.readFile;
import static java.nio.charset.StandardCharsets.UTF_8;

public class PasswordKeyStoreReader extends KeyStoreReader {

    @Override
    public void read(Path file, VaultKeys keys) {
        byte[] fileContents = readFile(file);
        byte[] decodedFileContents = Base64.getDecoder().decode(fileContents);
        char[] contents = UTF_8.decode(ByteBuffer.wrap(decodedFileContents)).array();
        keys.addPrivate(entryName(file), new PasswordKey(contents.clone()));

        Arrays.fill(contents, '0');
        Arrays.fill(fileContents, (byte) 0);
        Arrays.fill(decodedFileContents, (byte) 0);
    }

    @Override
    public List<String> getKeyExtensions() {
        //typo yes, possible to change: no without generating all secret for all teams again
        return Arrays.asList("password_alfa_numeric", "password_alfa_numeric_special_chars"); //NOSONAR
    }
}
