package nl.ing.lovebird.vault;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class KeyStoreHelper {

    private static final String X509_FACTORY_TYPE = "X.509";
    private static final String KEY_GEN_ALGORITHM = "RSA";
    public static final String KEY_STORE_PASSWORD = "dontchangeit";
    private static final char[] KEY_STORE_PASSWORD_ARRAY = KEY_STORE_PASSWORD.toCharArray();

    private PrivateKey privateKey;
    private X509Certificate certificate;
    private X509Certificate issuingCACertificate;
    private String alias;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static KeyStoreHelper newInstanceForKeyStore(String vaultSecretsDirectory, String alias, String certificateFileName, String issuingCaFileName, String privateKeyFileName) {
        KeyStoreHelper helper = newInstanceForTrustStore(vaultSecretsDirectory, alias, certificateFileName, issuingCaFileName);
        helper.privateKey = createPrivateKeyFromPem(vaultSecretsDirectory, privateKeyFileName);
        return helper;
    }

    public static KeyStoreHelper newInstanceForTrustStore(String vaultSecretsDirectory, String alias, String certificateFileName, String issuingCaFileName) {
        KeyStoreHelper helper = new KeyStoreHelper();
        helper.certificate = readPem(new File(vaultSecretsDirectory, certificateFileName));
        helper.issuingCACertificate = readPem(new File(vaultSecretsDirectory, issuingCaFileName));
        helper.alias = alias;
        return helper;
    }

    public KeyStore buildKeyStore() {
        try {
            return createKeyStore(alias, privateKey, certificate, issuingCACertificate);
        } catch (Exception e) {
            log.error("Couldn't load from certificates from file system. Review your pod and check if the certs are present", e);
            throw new IllegalStateException(e);
        }
    }

    public KeyStore buildTrustStore() {
        try {
            return createTrustStore(issuingCACertificate, certificate);
        } catch (final Exception e) {
            log.error("Couldn't load from certificates from file system. Review your pod and check if the certs are present", e);
            throw new IllegalStateException(e);
        }
    }

    private static PrivateKey createPrivateKeyFromPem(String vaultSecretsDirectory, String privateKeyFileName) {
        try (PemReader pemReader = new PemReader(new FileReader(new File(vaultSecretsDirectory, privateKeyFileName)))) {
            PemObject pemObject = pemReader.readPemObject();
            KeyFactory kf = KeyFactory.getInstance(KEY_GEN_ALGORITHM, "BC");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(pemObject.getContent()));
        } catch (IOException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    private KeyStore createKeyStore(String keyAlias, PrivateKey privateKey, X509Certificate... certificates) throws GeneralSecurityException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, KEY_STORE_PASSWORD_ARRAY);

        List<X509Certificate> certChain = new ArrayList<>();
        Collections.addAll(certChain, certificates);

        keyStore.setKeyEntry(keyAlias, privateKey, KEY_STORE_PASSWORD_ARRAY, certChain.toArray(new Certificate[certChain.size()]));

        return keyStore;
    }

    private KeyStore createTrustStore(X509Certificate... certificates) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, KEY_STORE_PASSWORD_ARRAY);

        int counter = 0;
        for (X509Certificate certificate : certificates) {
            keyStore.setCertificateEntry(String.format("cert_%d", counter++), certificate);
        }

        return keyStore;
    }

    private static X509Certificate readPem(File pemCertificate) {
        try (PemReader reader = new PemReader(new FileReader(pemCertificate))) {
            PemObject pemObject = reader.readPemObject();
            if (pemObject == null) {
                throw new IllegalStateException("Unable to construct PemObject from supplied PEM string");
            }
            return ((X509Certificate) CertificateFactory.getInstance(X509_FACTORY_TYPE)
                    .generateCertificate(new ByteArrayInputStream(pemObject.getContent())));
        } catch (IOException | CertificateException e) {
            throw new IllegalStateException("Unable to construct X509Certificate from supplied PEM string", e);
        }
    }
}
