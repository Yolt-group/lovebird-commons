package nl.ing.lovebird.secretspipeline;

import com.yolt.securityutils.crypto.RSA;
import org.assertj.core.util.Lists;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import static nl.ing.lovebird.secretspipeline.converters.KeyStoreReader.TYPE_INDICATOR;
import static org.assertj.core.api.Assertions.assertThat;

class KeyUtilsTest {

    private static final String POD_CERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIEbzCCA1egAwIBAgIUNCtsvO5DKNu1DpAAFHGgx8CzyIEwDQYJKoZIhvcNAQEL\n" +
            "BQAwEjEQMA4GA1UEAxMHZGVmYXVsdDAeFw0yMDAzMTIxMDQzMTVaFw0yMTAzMTIx\n" +
            "MDQ0MTVaMEcxCzAJBgNVBAYTAk5MMRYwFAYDVQQIEw1Ob29yZC1Ib2xsYW5kMQ0w\n" +
            "CwYDVQQKEwRZb2x0MREwDwYDVQQDEwhwYXJ0bmVyczCCASIwDQYJKoZIhvcNAQEB\n" +
            "BQADggEPADCCAQoCggEBALNOfSHbInlcT2qA/OcNzo5Ey67/BGssNKnCvceolIcz\n" +
            "dB/dCE2Ohwo+qjbrM0EjVEEF/Kn2FxfGSErdF3+Q0UJy8cqmgxJaUjVxIRmorQpI\n" +
            "o6TxQifQwzO0toT7KM0GQ7G9CVNhU0wMYEW3M0in092BwEbXsYyYVY14SejyGE1p\n" +
            "5zs01MVwH1SP9T8bN/xPl1MzUrylsC+48+EO8pfai2/nJpr3ZY8+XMEqjfn6K88J\n" +
            "dInCC/Psaw08R1tMNQJ1k9rfhqs2jl2c9LLzOdQO+l4QXO5JIr9h3ECOWqgO6zlV\n" +
            "M6FSQUfiuok/lKE9qPZcsQBbbmZzKYDlKB9AMrQqf2kCAwEAAaOCAYYwggGCMA4G\n" +
            "A1UdDwEB/wQEAwIDqDATBgNVHSUEDDAKBggrBgEFBQcDATAdBgNVHQ4EFgQU7iRN\n" +
            "erQkYHsW8x0BH9M2+w+7VMEwHwYDVR0jBBgwFoAUZXdr3wTewBV2J5WNgPplzrfH\n" +
            "hA8wgbAGCCsGAQUFBwEBBIGjMIGgME8GCCsGAQUFBzABhkNodHRwczovL3ZhdWx0\n" +
            "LnZhdWx0LWR0YS55b2x0LmlvL3YxL3RlYW0xMC9rOHMvcG9kcy9kZWZhdWx0L3Br\n" +
            "aS9vY3NwME0GCCsGAQUFBzAChkFodHRwczovL3ZhdWx0LnZhdWx0LWR0YS55b2x0\n" +
            "LmlvL3YxL3RlYW0xMC9rOHMvcG9kcy9kZWZhdWx0L3BraS9jYTATBgNVHREEDDAK\n" +
            "gghwYXJ0bmVyczBTBgNVHR8ETDBKMEigRqBEhkJodHRwczovL3ZhdWx0LnZhdWx0\n" +
            "LWR0YS55b2x0LmlvL3YxL3RlYW0xMC9rOHMvcG9kcy9kZWZhdWx0L3BraS9jcmww\n" +
            "DQYJKoZIhvcNAQELBQADggEBAFOm6VUGlIvSopGfXLvaT5hFsnnfmgexNHg6e7gU\n" +
            "zn3Tif3C+EHZu6wwE/8blQJk8z4DdsfWC331fVyb18D4sq7f6La2TxeE3qwjm4Co\n" +
            "37TU8v7BORDYnDTBxsb2DEm1LiwLceoAe8hdayGzsuOmZuK5HvhRO/vDhDX0LZVu\n" +
            "Q2//u+7/J5kwDJbeDjoHzqO0QSGMT0HqTjhZlGDSnhxZYMf0M+7fDIqfcsytpbHt\n" +
            "PhXW6I5s2Wv3t5safYJBYwk77uVt9DatdUXhIU5NiJMyuMttWpzJpzLj2+3tgXlm\n" +
            "LZpWQ4w9Lh4GAkqsgJxjPjJgzd4jQKtCTl878bqyeoep6iA=\n" +
            "-----END CERTIFICATE-----";

    @Test
    void readKeyStoreShouldFail(@TempDir Path tempDir) throws Exception {
        Path keyStoreFile = tempDir.resolve("keystore.jks");
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, new char[0]);

        PemReader pemReader = new PemReader(new StringReader(POD_CERT));
        PemObject pemObject = pemReader.readPemObject();
        byte[] certBytes = pemObject.getContent();
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
        Certificate[] certChain = new Certificate[]{cert};

        keyStore.setKeyEntry("key1", RSA.Builder.generateKeys(1024).getPrivateKey().getKey(), "password".toCharArray(), certChain);
        keyStore.store(Files.newOutputStream(tempDir.resolve("keystore.jks")), "test".toCharArray());

        assertThat(KeyUtils.isSecretFile(keyStoreFile)).isFalse();
        assertThat(KeyUtils.getKeyType(keyStoreFile)).isEmpty();
    }

    @Test
    void validSecretFile(@TempDir Path tempDir) throws IOException {
        Path secretFile = tempDir.resolve("secret");
        Files.write(secretFile, Lists.list(TYPE_INDICATOR + "PASSWORD_ALFA_NUMERIC", "my-secret-password"));

        assertThat(KeyUtils.isSecretFile(secretFile)).isTrue();
        assertThat(KeyUtils.getKeyType(secretFile)).get().isEqualTo("PASSWORD_ALFA_NUMERIC");
    }

}