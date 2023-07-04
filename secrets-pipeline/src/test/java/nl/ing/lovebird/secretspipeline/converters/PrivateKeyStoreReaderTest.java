package nl.ing.lovebird.secretspipeline.converters;

import com.yolt.securityutils.crypto.KeyPair;
import com.yolt.securityutils.crypto.PrivateKey;
import com.yolt.securityutils.crypto.PublicKey;
import com.yolt.securityutils.crypto.RSA;
import nl.ing.lovebird.secretspipeline.VaultKeys;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.Security;
import java.util.Arrays;

import static nl.ing.lovebird.secretspipeline.converters.KeyStoreReader.TYPE_INDICATOR;
import static nl.ing.lovebird.secretspipeline.converters.TestUtil.toPem;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

class PrivateKeyStoreReaderTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final VaultKeys keys = new VaultKeys();

    @Test
    void newCreatedPrivateAndPublicKey(@TempDir Path tempDir) throws Exception {
        KeyPair keyPair = RSA.Builder.generateKeys(2048);
        Path testFile = TestUtil.createAsymmetricFile(tempDir, keyPair.getPrivateKey().toPem(),
                keyPair.getPublicKey().toPem(),
                "testRSA", "rsa_2048",
                null,
                true);

        new PrivateKeyStoreReader().read(testFile, keys);

        assertThat(keys.getPrivateKey("testRSA")).isEqualTo(keyPair.getPrivateKey());
        assertThat(keys.getPublicKey("testRSA")).isEqualTo(keyPair.getPublicKey());
    }

    @Test
    void withExistingRSAKeyPair(@TempDir Path tempDir) throws Exception {
        String privateKey = "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JSUV2Z0lCQURBTkJna3Foa2lHOXcwQkFRRUZBQVNDQktnd2dnU2tBZ0VBQW9JQkFRQytxMUF3bTlzOVNHTjEKRTNyRVBFa3A1ZC9RdkpJcUFRc3Q3T21heXZEZ05TWTVGYk1LSGoxbUJNMm5uQVBkSkw1VmNyUFNIeUNrdE9YRQpXWnJuTDF0Q3hQUWtUZ3JDUllxNkdwRW9PcjU3S3FQYURqeEM1dUl3YW9ZY1BqVk9vQ1B4TzJTNjFjVnc2TGh0CnEreXFtUTNlbkhlNDRoWFdtckFLa015Y2todHVPNitxNStFbGFFd3REL2Z1NHFuV3cwZFZSVmprQTlMS1p0RUgKakVqRGtHNkVIWXVuTVcxb0VEeTRkc05lakRWbUU5WUZPY0JPOFdYSCt0MytvQXZPRTA5ZFg0Y3pRMXFlTU1sNgppYXRNT2IxUDZQVGVjeHhlNlFIVnMva25FK1RjS09xczBaQTZkRU40MS9TekNxSUlIZmtjQUZFaTVpOThqc0hVCm9IbURIVmovQWdNQkFBRUNnZ0VBRjF1TVFOQXZEUFJidnNGcXBqU09iL1FKdFp3TU9kb1Q0UVhuWGoralA0d3oKU1FYMy8zTFI2YmdINjVNSXhDUVdyd2dVc0ZhQTNMeU9jejNZNUo1Z0N5OXVpeTRHVW1OTGlkM3NUMlhXNU9iRgoyTllxbXJXSEd4QkpxcGNTVjdYMWdUMVo2eXQ1K3dsL1ltd1AyNXhSWHlhQzRpTE1NeFNUSVZBNHlLdDdjY2JtCnl6aGE5VFVzc0dwZlEvVStYYlh5RHEweUV4SWZReVNwTHk4V2NONzdkYUlsbTRkbElsUjljQ3VJN2xQL1ZmYzcKVm5NS3IyNk4yanJ2cGoyRzRQM1ZIelNxZmovTnI1MlF2c3BVdVFsY0RSV3k0TlppVTkvZHlNaHhGekNNTUlmTwpqYldNMnV2T1Q2V3BXMjdDZGhXYnFJUC9rOGIwSERHTzFwUUY2TkM5VlFLQmdRRHROMVk5M3pRNUhKaGt6MTE3CjM5V0IyUTJxVWtsTWt0Q2JrM2pBQkVhNE5Sckp1Nmx2ZVY0VnlpZGczQlZrLythZFNHcGFOODM0aWsrNk4wRVYKc2VJYTFHcktCclloOW9TYUw1OFRSc2tBZ0pwNU9BamJ0bWdkTm1mR0FlMUtVRi9EQmQvczZpOE1MaGJRbkprcgpnaTk4aVNPb3hpQnhydlJKYmNncHRldXo0d0tCZ1FETnhHbUF5WDNRcUhPaGhuY1hZL1JVMjhuMy9zSUYzYXRuCkh4Y2lJMW1VdnBnR3FWblV4cHhaWXRMcHVuRm5janBBNVFubjh0Z1I4T2lpODd3NXJVVERqMnQyTG9EUnp0WkgKZTZzVFlNYS9rZHAvVk8vWjhhRDNPU1J4QkRsQzF3bXNva1RzVlpjYjJ2SVdJbVdheUhIRmFnclhsK09LK0xWZAprS0x6UWFOcE5RS0JnUURyRGlaYThNMml1ZjhZQlpLd3FMKzhCd1JQZlhJOGVrNVRtdkhQODJ6UUkyOEQ3dnlhCkJUMkJnZXM3UTFtLzF3TUJYenhqWVdDcm93SVZsNkxhaUlWdjZJUlk1QVdkNmRURG8rU1JDVmNyTkVQZFVmVVYKSWM1UkJCUVlWTmg1QnVPZE1Ea1BYWTU1RjBKc1BJQmFhSW45Y1Rid2lrS2o0NlVWRGIrRjlVbzEyUUtCZ0FKSQpWbjlHUE80T29GRy9ZeHpRVUFxTThaMmRwdHRYelNuMHIxWUNuR0RKQ0hpeWNqdXBCRno5ZXFkN0dQenp5RytsCjJzbEc2Vnl4bG5NUVR4czRyOVdYaHJLbDYybm9hSG9jaGhaTjhZWjRnTHIrM3paRXR1dG5KOTlWRTZtSkp5TlMKSFgrZHAzSjRxRXpVanRuN3RUOWlZTUpwZmUyeHVwbFNIR3lrWXNLdEFvR0JBSTVhUzd0S3FHdEcwenc2VTlPbQo3SVEwM240Qk94aUJXekVpMkoxaXhxVGRPVFRYNzFZYW9MZ0djQ1NzTGN0V3JHTFZKNk1kQmZsQ09RQnZOaVhYCmcvMmYwb0t5eVNLQnN0MVZvdFovOGYyME54V0F0bmtjckRFV3g5KzFnZXFoV1FHbzdLSjZGZk95d3R4WlhSZk4KVUJJVy9naFNNeGFtTWR5UmtUdFNVcUdCCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K";
        String publicKey = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF2cXRRTUp2YlBVaGpkUk42eER4SgpLZVhmMEx5U0tnRUxMZXpwbXNydzREVW1PUld6Q2g0OVpnVE5wNXdEM1NTK1ZYS3owaDhncExUbHhGbWE1eTliClFzVDBKRTRLd2tXS3VocVJLRHErZXlxajJnNDhRdWJpTUdxR0hENDFUcUFqOFR0a3V0WEZjT2k0YmF2c3Fwa04KM3B4M3VPSVYxcHF3Q3BETW5KSWJianV2cXVmaEpXaE1MUS8zN3VLcDFzTkhWVVZZNUFQU3ltYlJCNHhJdzVCdQpoQjJMcHpGdGFCQTh1SGJEWG93MVpoUFdCVG5BVHZGbHgvcmQvcUFMemhOUFhWK0hNME5hbmpESmVvbXJURG05ClQrajAzbk1jWHVrQjFiUDVKeFBrM0NqcXJOR1FPblJEZU5mMHN3cWlDQjM1SEFCUkl1WXZmSTdCMUtCNWd4MVkKL3dJREFRQUIKLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==";
        String keyPair = privateKey + "----------\n" + publicKey;
        Path createdFile = tempDir.resolve("stubs-example-key2");
        Files.write(createdFile, Arrays.asList(TYPE_INDICATOR + "rsa_2048" + "\n"));
        Files.write(createdFile, keyPair.getBytes(UTF_8), StandardOpenOption.APPEND);

        new PrivateKeyStoreReader().read(createdFile, keys);

        assertThat(keys.getPublicKey("stubs-example-key2")).isEqualTo(PublicKey.createPublicKeyFromPem(new String(Base64.decode(publicKey.getBytes(UTF_8)))));
        assertThat(keys.getPrivateKey("stubs-example-key2")).isEqualTo(PrivateKey.from(new String(Base64.decode(privateKey.getBytes(UTF_8)))));
    }

    @Test
    void importExistingBase64EncodedPrivatePublicKey(@TempDir Path tempDir) throws Exception {
        String existingImportStringPrivateKey = "LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFcEFJQkFBS0NBUUVBMlBmT3g1YXVUYjI1Y0E2MkxvMWpEVzhUcEhRMi91dW00K0dHaTEyY0M1TEFrdk0wCk5hTkd6QXlDYlBRWUpFQVpEZUt6UHl4QmtGa01ReG5aQjlBNlVZQzJkRHMvL1hMYXV0cENOdUFiazhxL1QzZjUKMzN6Y1lRbkZubksxWUpyVzZCa0hxcTE5QU9HamM4bXMySURiYzlsRjlmYTE0VW1xZVM1T3VQYUQ1U1dvdXpwVQo3amVxT2QxUTJaaCtWMGhBWUdEZFpTL0VLN1JmcXlvWTVyaENTbm1ja1o2Y0pySGQ1WmdXRjU1dE1TQVpId1FvCmhBN01XM3VSVm81ZUNvMmc5RTFTLys5UEQ5eVRJZGt4QTBqeit6V0VEeGNraXdpOEZ5WldOVzBCSjV0ckNuL1UKT2daTGNOOHptZ1ViUmxRcjFsOS9UUnZSUEIvbGgvL002aThKOFFJREFRQUJBb0lCQUVKWVJGcXpzTWtFNHRkcApLRHhENzY0RDhUV3djOEN2dm5DZ1hjdnY2KzkrQ0hUbStXZ05iWENud3BvZWFlTnIxajF3dk83c1VZQWFPcTJxCkVidFo2UHRUS0hlU1JwYnhzQURERE14SzZ5UGVNM0hSZGc3bzExcTA2a0R4UXBVdnc5ZGxEL3NZNGNpTE5yZ0oKUzFsSXpkSTI5aldvSXlXb3FRQTFNTTFPVkRhVkpSUzZqdHM0eUl0OCtZQzMrTEl2MWRzaW5IRkY5dCtKRCtoVgp0cytEWVNGbmIxaEp2c3dweHdqdldXUkNHcVR2OEp6K29nZHZHYXdWVzNFY09CejdkV3NNUndvV0ZOcGI2WCtaCkJuc0lzMCtiZ3VlVUFKSnllZXp3WEVySVJPV0JzWTJMTWZNUUtwdXAyVFc3eklTWUNkKzc3NklvVVRPZGNNdy8KQ01mSUdLRUNnWUVBK0pOMVZRQVlKTzAyZXhGelpIOE5hc2xvdUtSZFc2L0hCcHdXK2hycjBBaTVYZmdqdWxRRQpJTzRDbnVuL1FRejErZmx3NTJ4ODJ2aFV2bEtRLzhiUnptSERsbnlpMVltU2hIb0tzNFQ4d0hkU09LTStpTmVjCnBaOTZFVFI2YWJZMWVYa2ZkZXJkaS9jYkhkN005bFZZTzkrN3pkZ2J4LzRVM1BVV1ZpMjJ0TE1DZ1lFQTMzS3UKKzF3WEY3RHY4WUdjY29YYWRweXZzVGo1STI2S1U5UTlpdG91SXhpcFBhN3RXbGFiR1FLK2FvL2pxRkk0eEZXRAp1Nlk0Z2ZRVDBtd0hlRThpUzBqVlZaOXdkZ3RXay9uV1lrSHBGN2JMM0JjOSsvMHJJei9ZVE5VbVRvRHpNS0hmCkdQRE9PMzRBVVdzOUJWdWVQbG14NkEzSWdFQUFqWnhyRmZqYlFNc0NnWUVBbzRjT2gwNjZrNEJpZU8vd0E4cjUKbnFqRmRjTGJqL2ZCYlAyK3QrYUIvNmZkbW80bG91eW1rcXRCbWgzQ1NKOG9LcjBJbzVaaFJRUmRhNStSdUZLcApyNlh0TDlEcXBxUU1lWTUzLzhXYktWcldBbUNTUVIvbTg2WklmWTBTZ3Z6VmpRWTd4aTB1ZG1lbnRTemZsYVpyCmwyZ0dldEtTNUN5b3VQdWJLREtHb2JVQ2dZRUEzb2pEMWhnWDg0Tkc0L25XU29RZVY2U2lvVmlCWHhVMjlGZmEKMXMrRnhYUkpFT0tIQmtKY2d1TWxxdEVVeE16bU5qcUlVTGt4YTlNZWJxRWlLMko4WUFmd1Z5N0wvUFE3ekhHMApYZlVRWklxcnFzMEc4VkJveHRsTjNPOG5FR1lDVFB2WlNXaUJxK1JJQXM1UFhtZXBTS1ZodGg4TUFSWTh0c3dDCkprYktiVk1DZ1lBVkhwZ0JCWFlEZzhIRGtEMHVMcDVFRFVxN1phN0pYUG9BWTJhc012eklvUnZMVmtvOW5KamMKbEx4SHVNT1JQbmFLL3ZuVjRIMjV5eXRseTlGdVg4TDF0TmRZOXdvdkp2T1hQL0JZcS9aK25TenE5QXBXQVNSQgo4dWZveHREYlhJalk5aUxEYmY2TUZXakVmZE1XdmtxT0xzV2RFL3IxS1Z6ZGZGT2VEMGdTR3c9PQotLS0tLUVORCBSU0EgUFJJVkFURSBLRVktLS0tLQo=";
        String existingImportStringPublicKey = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF0WXkrekkyaGMwWm1lK3RCZXdqVgp0UUg0YnpURzV4MHhqQ2haWjZpQmR4aTNsNEZ0ZUpYZE9td2pvSWg2ZlYvc2FUajBzVDdCNU5EdEJqam5sNWtUCmdiZG9CR3hVTWtNVFh4d1NGekVqTkk5YXkzWitpRGNSTWF4QnJhdDBKT3hVSVlJcEtiNGxsSlNkMGF3a1FDWkgKOTJGOVVmbURpVW91cG50U3liUWZiK2FxV2VhNmE0MmVOcjdkSW5kYkhWM29TalV4TkFreVBlbEpSUldKQ3BZaQpjc2xmRjY0M2pCeGlLU09YYnJzWkNyajdOaVBhNVQzSk54ODhHempiaU1wSExobS9jQVZ3NExQK0RhWS90bnIrCjdGeng0eEMrejE3ZlZ2MmorNHNHVGtEVHEySFdMZWZwSFBmYTZ3aWgzT3JNcStBazBDZUh5K3BtNUd3cUxBWEoKQlFJREFRQUIKLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==";
        Path testFile = TestUtil.createAsymmetricFile(tempDir, existingImportStringPrivateKey,
                existingImportStringPublicKey,
                "testRSA2", "rsa_2048",
                null,
                false);

        new PrivateKeyStoreReader().read(testFile, keys);

        assertThat(keys.getPublicKey("testRSA2")).isEqualTo(PublicKey.createPublicKeyFromPem(new String(Base64.decode(existingImportStringPublicKey.getBytes(UTF_8)))));
        assertThat(keys.getPrivateKey("testRSA2")).isEqualTo(PrivateKey.from(new String(Base64.decode(existingImportStringPrivateKey.getBytes(UTF_8)))));
    }

    @Test
    void importPemEncodedPrivateKeyAndCert(@TempDir Path tempDir) throws Exception {
        String privateKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
                "MIIEpAIBAAKCAQEA2PfOx5auTb25cA62Lo1jDW8TpHQ2/uum4+GGi12cC5LAkvM0\n" +
                "NaNGzAyCbPQYJEAZDeKzPyxBkFkMQxnZB9A6UYC2dDs//XLautpCNuAbk8q/T3f5\n" +
                "33zcYQnFnnK1YJrW6BkHqq19AOGjc8ms2IDbc9lF9fa14UmqeS5OuPaD5SWouzpU\n" +
                "7jeqOd1Q2Zh+V0hAYGDdZS/EK7RfqyoY5rhCSnmckZ6cJrHd5ZgWF55tMSAZHwQo\n" +
                "hA7MW3uRVo5eCo2g9E1S/+9PD9yTIdkxA0jz+zWEDxckiwi8FyZWNW0BJ5trCn/U\n" +
                "OgZLcN8zmgUbRlQr1l9/TRvRPB/lh//M6i8J8QIDAQABAoIBAEJYRFqzsMkE4tdp\n" +
                "KDxD764D8TWwc8CvvnCgXcvv6+9+CHTm+WgNbXCnwpoeaeNr1j1wvO7sUYAaOq2q\n" +
                "EbtZ6PtTKHeSRpbxsADDDMxK6yPeM3HRdg7o11q06kDxQpUvw9dlD/sY4ciLNrgJ\n" +
                "S1lIzdI29jWoIyWoqQA1MM1OVDaVJRS6jts4yIt8+YC3+LIv1dsinHFF9t+JD+hV\n" +
                "ts+DYSFnb1hJvswpxwjvWWRCGqTv8Jz+ogdvGawVW3EcOBz7dWsMRwoWFNpb6X+Z\n" +
                "BnsIs0+bgueUAJJyeezwXErIROWBsY2LMfMQKpup2TW7zISYCd+776IoUTOdcMw/\n" +
                "CMfIGKECgYEA+JN1VQAYJO02exFzZH8NaslouKRdW6/HBpwW+hrr0Ai5XfgjulQE\n" +
                "IO4Cnun/QQz1+flw52x82vhUvlKQ/8bRzmHDlnyi1YmShHoKs4T8wHdSOKM+iNec\n" +
                "pZ96ETR6abY1eXkfderdi/cbHd7M9lVYO9+7zdgbx/4U3PUWVi22tLMCgYEA33Ku\n" +
                "+1wXF7Dv8YGccoXadpyvsTj5I26KU9Q9itouIxipPa7tWlabGQK+ao/jqFI4xFWD\n" +
                "u6Y4gfQT0mwHeE8iS0jVVZ9wdgtWk/nWYkHpF7bL3Bc9+/0rIz/YTNUmToDzMKHf\n" +
                "GPDOO34AUWs9BVuePlmx6A3IgEAAjZxrFfjbQMsCgYEAo4cOh066k4BieO/wA8r5\n" +
                "nqjFdcLbj/fBbP2+t+aB/6fdmo4louymkqtBmh3CSJ8oKr0Io5ZhRQRda5+RuFKp\n" +
                "r6XtL9DqpqQMeY53/8WbKVrWAmCSQR/m86ZIfY0SgvzVjQY7xi0udmentSzflaZr\n" +
                "l2gGetKS5CyouPubKDKGobUCgYEA3ojD1hgX84NG4/nWSoQeV6SioViBXxU29Ffa\n" +
                "1s+FxXRJEOKHBkJcguMlqtEUxMzmNjqIULkxa9MebqEiK2J8YAfwVy7L/PQ7zHG0\n" +
                "XfUQZIqrqs0G8VBoxtlN3O8nEGYCTPvZSWiBq+RIAs5PXmepSKVhth8MARY8tswC\n" +
                "JkbKbVMCgYAVHpgBBXYDg8HDkD0uLp5EDUq7Za7JXPoAY2asMvzIoRvLVko9nJjc\n" +
                "lLxHuMORPnaK/vnV4H25yytly9FuX8L1tNdY9wovJvOXP/BYq/Z+nSzq9ApWASRB\n" +
                "8ufoxtDbXIjY9iLDbf6MFWjEfdMWvkqOLsWdE/r1KVzdfFOeD0gSGw==\n" +
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
        Path testFile = TestUtil.createAsymmetricFile(tempDir, privateKey, cert, "testcert", "rsa_2048", null, true);

        new PrivateKeyStoreReader().read(testFile, keys);

        assertThat(keys.getPrivateKey("testcert")).isEqualTo(PrivateKey.from(privateKey));
        assertThat(cert).isEqualToIgnoringNewLines(toPem(keys.getCertificate("testcert")));
    }

    /**
     * Test below fails, the original code only created an entry which works fine only putting it in the key store and
     * getting it from the key store still fails the proposed patch in the original code does not work see
     */
    @Test
    void privateKeyAndCertECC(@TempDir Path tempDir) throws Exception {
        String privateKey = "-----BEGIN EC PRIVATE KEY-----\n" +
                "MHcCAQEEIOV5qU54W/p3JOfWU4AtzFjwaX047sVR+c5eoOdmlSBeoAoGCCqGSM49\n" +
                "AwEHoUQDQgAETWqhj7e+SNEJtttSe5ReqdApaN86FA88fo+la4DkRPT3Wzz71Ffp\n" +
                "ufefQzQJmp5cBoXzsSsElMBgDFH3K7ZTQg==\n" +
                "-----END EC PRIVATE KEY-----\n";
        String certificate = "-----BEGIN CERTIFICATE-----\n" +
                "MIIBsjCCAVgCCQDp0BxwWOnINDAKBggqhkjOPQQDAjBhMQswCQYDVQQGEwJubDEL\n" +
                "MAkGA1UECAwCbmwxCzAJBgNVBAcMAm5sMQswCQYDVQQKDAJubDELMAkGA1UECwwC\n" +
                "bmwxCzAJBgNVBAMMAm5sMREwDwYJKoZIhvcNAQkBFgJubDAeFw0yMDA1MDYwODA5\n" +
                "MDRaFw0yMTA1MDYwODA5MDRaMGExCzAJBgNVBAYTAm5sMQswCQYDVQQIDAJubDEL\n" +
                "MAkGA1UEBwwCbmwxCzAJBgNVBAoMAm5sMQswCQYDVQQLDAJubDELMAkGA1UEAwwC\n" +
                "bmwxETAPBgkqhkiG9w0BCQEWAm5sMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE\n" +
                "TWqhj7e+SNEJtttSe5ReqdApaN86FA88fo+la4DkRPT3Wzz71FfpufefQzQJmp5c\n" +
                "BoXzsSsElMBgDFH3K7ZTQjAKBggqhkjOPQQDAgNIADBFAiBWIfz9LJG2iiuHptFJ\n" +
                "I+V/pUvMh8ZTA3sh8cjvSEHoqAIhAM66GlgY03gQf5cbxslXXNFrPk6sumSc1S7U\n" +
                "ypgoeOU1\n" +
                "-----END CERTIFICATE-----\n";
        Path testFile = TestUtil.createAsymmetricFile(tempDir, privateKey, certificate, "testcertECC", "rsa_2048", null, true);

        new PrivateKeyStoreReader().read(testFile, keys);

        assertThat(keys.getPrivateKey("testcertECC").toPem()).isEqualToIgnoringNewLines(privateKey);
        assertThat(TestUtil.toPem(keys.getCertificate("testcertECC"))).isEqualToIgnoringNewLines(certificate);
    }

}
