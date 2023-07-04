package nl.ing.lovebird.secretspipeline.converters;

import nl.ing.lovebird.secretspipeline.VaultKeys;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.List;

import static nl.ing.lovebird.secretspipeline.KeyUtils.readFile;
import static nl.ing.lovebird.secretspipeline.KeyUtils.splitAsymmetric;

public class CertificateKeyStoreReader extends KeyStoreReader {

    @Override
    public void read(Path file, VaultKeys keys) throws Exception {
        byte[][] fileContents = splitAsymmetric(readFile(file));
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509", BouncyCastleProvider.PROVIDER_NAME);
        InputStream in = new ByteArrayInputStream(Base64.decode(fileContents[1]));
        keys.addPublic(entryName(file), certFactory.generateCertificate(in));
    }

    @Override
    public List<String> getKeyExtensions() {
        return Arrays.asList("cert_any_import");
    }
}
