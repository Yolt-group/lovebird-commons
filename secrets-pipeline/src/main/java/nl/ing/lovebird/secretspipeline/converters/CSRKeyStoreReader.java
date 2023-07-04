package nl.ing.lovebird.secretspipeline.converters;

import com.yolt.securityutils.crypto.PrivateKey;
import com.yolt.securityutils.crypto.PublicKey;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.secretspipeline.VaultKeys;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;

import java.io.StringReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static nl.ing.lovebird.secretspipeline.KeyUtils.readFile;
import static nl.ing.lovebird.secretspipeline.KeyUtils.splitAsymmetric;

@Slf4j
public class CSRKeyStoreReader extends KeyStoreReader {

    public static PublicKey createPublicKeyFromCSR(String csr) throws Exception {
        try (PEMParser pemParser = new PEMParser(new StringReader(csr))) {
            Object parsedObj = pemParser.readObject();
            if (parsedObj instanceof PKCS10CertificationRequest) {
                JcaPKCS10CertificationRequest jcaPKCS10CertificationRequest = new JcaPKCS10CertificationRequest((PKCS10CertificationRequest) parsedObj);
                return PublicKey.from(jcaPKCS10CertificationRequest.getPublicKey());
            }
        }
        return null;
    }

    @Override
    public void read(Path file, VaultKeys keys) throws Exception {
        byte[][] fileContents = splitAsymmetric(readFile(file));
        keys.addPrivate(entryName(file), PrivateKey.from(new String(Base64.decode(fileContents[0]))));
        keys.addPublic(entryName(file), createPublicKeyFromCSR(new String(Base64.decode(fileContents[1]))));

        Arrays.fill(fileContents[0], (byte) 0);
        Arrays.fill(fileContents[1], (byte) 0);
    }

    @Override
    public List<String> getKeyExtensions() {
        return Arrays.asList("csr");
    }
}
