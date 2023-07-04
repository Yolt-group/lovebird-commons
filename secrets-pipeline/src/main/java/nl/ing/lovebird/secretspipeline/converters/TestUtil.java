package nl.ing.lovebird.secretspipeline.converters;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.ing.lovebird.secretspipeline.converters.KeyStoreReader.TYPE_INDICATOR;

public class TestUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static Path createSymmetricKeyFile(byte[] key, Path directory, String fileName, String type, boolean encode) throws IOException {
        byte[] content;
        if (encode) {
            content = Base64.encodeBase64(key);
        } else {
            content = key;
        }
        Path createdFile = directory.resolve(fileName);
        Files.write(createdFile, Arrays.asList(TYPE_INDICATOR + type.toUpperCase()));
        Files.write(createdFile, content, StandardOpenOption.APPEND);
        return createdFile;
    }

    public static KeyStore createKeyStore() {
        try {
            KeyStore keyStore = KeyStore.getInstance("UBER", "BC");
            keyStore.load(null, "test".toCharArray());
            return keyStore;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static Path createAsymmetricFile(Path directory, String privatePart, String publicPart, String fileName, String type, String error, boolean encode) throws IOException {
        if (encode) {
            privatePart = Base64.encodeBase64String(privatePart.getBytes(UTF_8));
            publicPart = Base64.encodeBase64String(publicPart.getBytes(UTF_8)); //transit backend does not encode public key.
        }
        Path createdFile = directory.resolve(fileName);
        Files.write(createdFile, Arrays.asList(TYPE_INDICATOR + type));
        Files.write(createdFile, (privatePart + "----------\n" + publicPart).getBytes(UTF_8), StandardOpenOption.APPEND);
        if (StringUtils.hasLength(error)) {
            Files.write(createdFile, error.getBytes(UTF_8), StandardOpenOption.APPEND);
        }
        return createdFile;
    }

    public static String toPem(Certificate cert) throws IOException {
        StringWriter sw = new StringWriter();
        try (JcaPEMWriter pw = new JcaPEMWriter(sw)) {
            pw.writeObject(cert);
            pw.flush();
        }
        return sw.toString();
    }

}
