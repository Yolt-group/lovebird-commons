package nl.ing.lovebird.secretspipeline.converters;

import com.yolt.securityutils.crypto.PrivateKey;
import com.yolt.securityutils.crypto.PublicKey;
import nl.ing.lovebird.secretspipeline.VaultKeys;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.List;

import static nl.ing.lovebird.secretspipeline.KeyUtils.readFile;
import static nl.ing.lovebird.secretspipeline.KeyUtils.splitAsymmetric;

@Slf4j
public class PrivateKeyStoreReader extends KeyStoreReader {

    @Override
    public void read(Path file, VaultKeys keys) throws Exception {
        byte[][] fileContents = splitAsymmetric(readFile(file));
        keys.addPrivate(entryName(file), PrivateKey.from(new String(Base64.decode(fileContents[0]))));
        String publicKeyPem = new String(Base64.decode(fileContents[1]));
        try {
            byte[] bytes = publicKeyPem.getBytes();
            CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");
            Certificate cert = cf.generateCertificate(new ByteArrayInputStream(bytes));
            keys.addPublic(entryName(file), cert);
        } catch (CertificateException e) {
            PublicKey publicKey = PublicKey.createPublicKeyFromPem(publicKeyPem);
            keys.addPublic(entryName(file), publicKey);
        }

        Arrays.fill(fileContents[0], (byte) 0);
        Arrays.fill(fileContents[1], (byte) 0);
    }

    @Override
    public List<String> getKeyExtensions() {
        return Arrays.asList("rsa_2048", "rsa_4096");
    }
}
