package nl.ing.lovebird.secretspipeline.converters;

import nl.ing.lovebird.secretspipeline.VaultKeys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.security.cert.Certificate;

import static org.assertj.core.api.Assertions.assertThat;

class CertificateKeyStoreReaderTest {

    private final VaultKeys vaultKeys = new VaultKeys();

    @Test
    void readCertificate(@TempDir Path tempDir) throws Exception {
        String privateKey = "-----BEGIN RSA PRIVATE KEY-----\n" + //NOTE; IT NEEDS RSA AS PRIVATE KEY NOMINATOR!
                "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDoTwHGES23JBYd\n" +
                "J4gQ8UEWN+P9RfT55uOXeADwXXRl5POIZPOPOhHQ7xN2WK8GC+mE6CVoDm6Pjfis\n" +
                "F9xSq3IfYHMl4gg8wE31SDrQO44zkYPLQiFrg6b/rm45a4vA1R0pkFnTypfVbrDW\n" +
                "7zSMLzBTPsPF3WWftFFT+M6zxCuEU1oVmmn1GC8VPo9OzI0li35qOY5zcUpP5IgQ\n" +
                "uwxM19AVBe2UM0vNZqrFWJNouYbD/CQ6UsY66lOYCjyvpdbeWcL3E5MtQKBxNs2A\n" +
                "Q0Ie77q9qJ887abt+ReJFl5gY65XawJDrg+uz4wbnabjForWYFpPBC7UMZorcPG0\n" +
                "STB+l0lHAgMBAAECggEAMuUMSG5/C36WcbC1eLDCR7Ha+yQWdaGF/ytFWWPAGoq8\n" +
                "aAdl7N0WBiY6p1Tqk3KMqJeLim6O/lhmQJ3BoUL6b7FbyNLqZxTif3hhmjlSAC5D\n" +
                "J4bHd5ySO7XlZCMRrR+DkhWT+HiMJzBnAc/KPWQhPDul/HVyzDhfEPyTnIK/3e9S\n" +
                "ArASRk+GDKGtKXd+fzsZT9TsZ+pfkLQwszr5VzSH1akIJhKQ2oY01RRl4+Wu7+ex\n" +
                "+jvKVTWNrMEn6ZX/njnPiMFpzB9zQyjthBXILjCNP4IdHFWsten61wFAi09d9gc7\n" +
                "3ixBtT95zxZTEGVaZtG+dy7bRh7daFqL/GEhvvO04QKBgQD8vPv0/aWNO7vdQ26X\n" +
                "CYIV4d7P9Gy2xlyNGDlyanLd4ytmW7rbdAwxGWn2FbqYdJKdNUyyfLm3Ujw1tcBG\n" +
                "T6iXTDXfV5qDuaZJXyI1KJs+t0MdtZ3nsVwZasbJGhD3/xDfpSWzJF2l4jTT+wiH\n" +
                "5bZ+5WQOMi05aNbSFenTVUxNHwKBgQDrToaegM/gJOZU/PwKdUzCcw+/carUmMMd\n" +
                "UjtFMI/399sRBm1nTsd/fzFsW71x7951DGjO8BRjUaEDU15ANl+fFlzayHffaEo4\n" +
                "Fc1IFhbyMx/+lCKMNxv/oltJmFd3zxzfu4WL59Dy6LWhwTbKs67ctylWWrstY3+x\n" +
                "l6YJ907W2QKBgClEfmuBYx/Nih5V3V/iGJCUIfqYsYuilggf1Xl+MiVS64o6Hiep\n" +
                "Kjh2KtwYrjAokKwEwPI/9V8emWA3vh588U1LD1zZhAxQKvu+AermJ3s1F0tplU/4\n" +
                "oM4xpiW+ENk9l7a804wNuxDT/8ZBLiJqesL7l7vNq569JJ6HH3f3bI8HAoGBAN8p\n" +
                "81yiGAShml1iLXuRsgH89COCHx1P4ESPt6ywGOp5BCfKa7n9qhEORYZaH3rOnrFt\n" +
                "2nzgjsV/JnCsAYhuyRNtNxU76QxVsYYtjypd4NdFi4N7ZKSMo543kdJjjBkRsXWz\n" +
                "OD3u1ml6MMC/KULnVOD5SVgf5sNuSS3aAFwVU41JAoGAKQghQe9yGheet3/etR0d\n" +
                "oxpzvIjm5dcEo/yj+E/XTe21mvh5FAE2bH6UWDWNgXjReUDxbh9XphF9snlyRCQt\n" +
                "YdXAXAs7rHhUrZo4ModQnfp4lFwNo12cUN8nSQaS3F7tZY+V6dWoAYM4HhqnbGtg\n" +
                "5Ucrnkx2Uk+jF2zHhLplraM=\n" +
                "-----END RSA PRIVATE KEY-----";
        String cert = "-----BEGIN CERTIFICATE-----\n" +
                "MIIDPjCCAiYCCQCtCVP29akXHDANBgkqhkiG9w0BAQsFADBhMQswCQYDVQQGEwJu\n" +
                "bDELMAkGA1UECAwCbmwxCzAJBgNVBAcMAm5sMQswCQYDVQQKDAJubDELMAkGA1UE\n" +
                "CwwCbmwxCzAJBgNVBAMMAm5sMREwDwYJKoZIhvcNAQkBFgJubDAeFw0yMDA1MDQx\n" +
                "OTQzMDZaFw0yMTA1MDQxOTQzMDZaMGExCzAJBgNVBAYTAm5sMQswCQYDVQQIDAJu\n" +
                "bDELMAkGA1UEBwwCbmwxCzAJBgNVBAoMAm5sMQswCQYDVQQLDAJubDELMAkGA1UE\n" +
                "AwwCbmwxETAPBgkqhkiG9w0BCQEWAm5sMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8A\n" +
                "MIIBCgKCAQEA6E8BxhEttyQWHSeIEPFBFjfj/UX0+ebjl3gA8F10ZeTziGTzjzoR\n" +
                "0O8TdlivBgvphOglaA5uj434rBfcUqtyH2BzJeIIPMBN9Ug60DuOM5GDy0Iha4Om\n" +
                "/65uOWuLwNUdKZBZ08qX1W6w1u80jC8wUz7Dxd1ln7RRU/jOs8QrhFNaFZpp9Rgv\n" +
                "FT6PTsyNJYt+ajmOc3FKT+SIELsMTNfQFQXtlDNLzWaqxViTaLmGw/wkOlLGOupT\n" +
                "mAo8r6XW3lnC9xOTLUCgcTbNgENCHu+6vaifPO2m7fkXiRZeYGOuV2sCQ64Prs+M\n" +
                "G52m4xaK1mBaTwQu1DGaK3DxtEkwfpdJRwIDAQABMA0GCSqGSIb3DQEBCwUAA4IB\n" +
                "AQBkzS+uG6t/nC+SmYvBPdNEeH/kHJyMCLtV9N9lqw5NH7zgTGE91Zswq3VuNOlE\n" +
                "TVDB4zvB7HaTkPg6u/FBIIU/huWEHgQ5UmucwUt+CI7Gd9juweKjnOsxGHsF3CuQ\n" +
                "x/neh/Suc0LQigkQ+fDPK9u6vKYaVV3cZFlVGYeKehsGpvOD5B++5VAD3zuNLxiv\n" +
                "fRAAOi/mfdPaOaCPwc6xnLmrykOKh5q5Q5WeCYGHsLFIkdoZrTASAvDFqKCMJYKP\n" +
                "QSU1KE2PHX8U1GI3yAr7BnKIzRIfiX330Y4WWQMj85pgippLLUecBgirfnMZ9JTu\n" +
                "70lv7x/Qc15vdB8WrnwPgNK4\n" +
                "-----END CERTIFICATE-----\n";
        Path testFile = TestUtil.createAsymmetricFile(tempDir, privateKey, cert, "testcert", "_cert_any_import", null, true);
        new CertificateKeyStoreReader().read(testFile, vaultKeys);

        Certificate entry = vaultKeys.getCertificate("testcert");
        assertThat(TestUtil.toPem(entry)).isEqualToIgnoringNewLines(cert);
    }

}
