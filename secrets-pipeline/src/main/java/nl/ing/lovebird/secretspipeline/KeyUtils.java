package nl.ing.lovebird.secretspipeline;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.secretspipeline.converters.KeyStoreReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

@UtilityClass
@Slf4j
public class KeyUtils {

    private static final byte[] SECRET_HEADER = KeyStoreReader.TYPE_INDICATOR.getBytes(UTF_8);

    public static byte[] readFile(Path file) {
        Optional<String> keyType = getKeyType(file);
        if (keyType.isPresent()) {
            try {
                List<String> lines = Files.readAllLines(file, UTF_8);
                lines.remove(0); //removes type indicator line
                return String.join("\n", lines).getBytes(UTF_8);
            } catch (IOException e) {
                log.error("Unable to read file: {}", file, e);
            }
        }
        return new byte[0];
    }

    public static Optional<String> getKeyType(Path file) {
        try {
            if (isSecretFile(file)) {
                List<String> lines = Files.readAllLines(file, UTF_8);
                if (lines.size() > 1) {
                    String typeLine = lines.get(0);
                    if (typeLine.contains(KeyStoreReader.TYPE_INDICATOR)) {
                        return Optional.of(typeLine.replace(KeyStoreReader.TYPE_INDICATOR, ""));
                    }
                }
            }
        } catch (IOException e) {
            log.error("Unable to read file: {}", file, e);
        }
        return Optional.empty();
    }

    //Try to see if the file is a secrets file form secrets-pipeline we use raw byte reader as not all files
    //are UTF-8 (for example a keystore which also resides in /vault/secrets) which will throw a malformed input exception
    public boolean isSecretFile(Path file) throws IOException {
        try (InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[SECRET_HEADER.length];
            if (is.read(buffer) != SECRET_HEADER.length) {
                return false;
            }
            return Arrays.equals(buffer, SECRET_HEADER);
        }
    }

    public static byte[][] splitAsymmetric(byte[] byteContent) {
        char[] charContent = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(byteContent)).array();
        //---------- is the end!
        //find the delimiter:
        int firstEnd = 0;
        for (int i = 0; i < charContent.length; i++) {
            if (charContent[i] == '-') {
                firstEnd = i;
                break;
            }
        }
        char[] bufferFirstContent = Arrays.copyOfRange(charContent, 0, firstEnd);
        CharBuffer charBuffer = CharBuffer.wrap(bufferFirstContent);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        char[] bufferSecondContent = Arrays.copyOfRange(charContent, firstEnd + 11, charContent.length);
        CharBuffer secondCharBuffer = CharBuffer.wrap(bufferSecondContent);
        ByteBuffer secondByteBuffer = StandardCharsets.UTF_8.encode(secondCharBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        byte[] bytes2 = Arrays.copyOfRange(secondByteBuffer.array(),
                secondByteBuffer.position(), secondByteBuffer.limit());
        Arrays.fill(bufferFirstContent, '0');
        Arrays.fill(bufferSecondContent, '0');
        return new byte[][]{bytes, bytes2}; //note the secret is encoded by the transit engine
    }
}
