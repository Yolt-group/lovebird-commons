package nl.ing.lovebird.vault;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class KeyStoreHelperTest {

    public static final String POD_CERT = "-----BEGIN CERTIFICATE-----\n" +
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
    public static final String ISSUING_CA = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDKTCCAhGgAwIBAgIUHfAfqFzDHGn9ex8Tgj08wA58FbQwDQYJKoZIhvcNAQEL\n" +
            "BQAwEjEQMA4GA1UEAxMHZGVmYXVsdDAeFw0xOTAyMjYxNDQzMzJaFw0yOTAyMjMx\n" +
            "NDQ0MDJaMBIxEDAOBgNVBAMTB2RlZmF1bHQwggEiMA0GCSqGSIb3DQEBAQUAA4IB\n" +
            "DwAwggEKAoIBAQCmgF+ZgM5Rk+6kYhykgVMkf5tDCvAaMpcG/EowDTCc4uEERkfe\n" +
            "Vr3tfMSLC401m7VqLdaQ9WcFjg2yxqUVQ8BgjsrOnLABooa5lIM+uIp79QJIAWmy\n" +
            "RI/zJhZ4U74bl8OqSqOIBS48aIXfghsKRAhw7yqLXhFR20LrkhgZHL3+Y/x1WXRx\n" +
            "Lnej7wQSPCHWvN0vXPmsfWcXLExYekbb8nTwPVE1zGWysk8AWZL0j02uDNjXAeiI\n" +
            "uvBudlR4Q15uJ/AKzwlTjJkXA8KVaRsQ9oIwssLSbjFa2JujvqWCCYiEQxRzd+WN\n" +
            "Y24xZe2r6Bpelghe1QxLp5/Q0snoTLIE8Y/rAgMBAAGjdzB1MA4GA1UdDwEB/wQE\n" +
            "AwIBBjAPBgNVHRMBAf8EBTADAQH/MB0GA1UdDgQWBBRld2vfBN7AFXYnlY2A+mXO\n" +
            "t8eEDzAfBgNVHSMEGDAWgBRld2vfBN7AFXYnlY2A+mXOt8eEDzASBgNVHREECzAJ\n" +
            "ggdkZWZhdWx0MA0GCSqGSIb3DQEBCwUAA4IBAQCNVFmgm6oQM6NEmjCHDdXXFU3f\n" +
            "pvgLIQQOBKh5mt2AIF0DkW+OU31Ffy+u+t3MBRHKTpONuSUF1ikx3qmL4IMr2qk8\n" +
            "2ZNPxQbgMk7dQx/4ZBz8D75zIj9LDjgY77RCDmOpmHwUftZCXYCmJG2UjFuk1ZCG\n" +
            "deI/mCPKc2t3t5soW/W0WkDDOVKbH09CVB5a62dpQ/og/RmTGhEWr8PJTs7vdjlP\n" +
            "OVbT6YYSfybncrB7tB4/gQDrT4Q0BwMA3VtcJQsOOVcjsHDacxxkHsuVTWYmkRwX\n" +
            "zihFHlTuRGwbuUyV5d0n75BVsNuU/EiictnV0XYaUgn1JlV3YIsiMacNPqN6\n" +
            "-----END CERTIFICATE-----";
    public static final String PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEowIBAAKCAQEAu/xm9hoWFdNQ0zbUKgdfcL/VJVJFlXYvPDIO1kKbv/x6/Wlk\n" +
            "asWwabgtQ+yKky6C1xzlMJNs82kyPBwxF1jRqh73qeWAtudaCOYyCykCpkfgLbFe\n" +
            "+Xd79g7hroKtcuF6TGREviWxCInwFFEHnf5TZTFypOUZHcWmEY7EGgeBXYBp40ZI\n" +
            "5V9Jkid5kxTVygfJt/YbEf43S1j4iNAa2AWty2tN3W0n1DGwO6F8U4akiHcyrOjz\n" +
            "dL2UejKdSdy8s1qCboW8Qhf8I6feFUmcOmM5ClfoWnZyeIlkqYAdrJhUFYkQsPgb\n" +
            "i7oSi7HhQkBdWzt6ykoKw36WrHQ9tNqfwP/WFQIDAQABAoIBAAM+gNxgaN8pjWyH\n" +
            "trOe+vOsK9aAC/lfV8NXLdBex+dRSSIUboo1LS0143oXm/CcTd++fOoQsUGmIBrP\n" +
            "db1sZ/ninO2Oq2D8rx4WMujkZUpPVTwUoon1mOsPKK/lS27/Gyg0VsddSSfXkZAY\n" +
            "MeR1HiVR7COSXJOZ9Jq69wFn/cCKTivlgPQrjEzwVXg/nrfzu+DKruloT2eGMJVX\n" +
            "VtDlENUe7224gbvOoXTw457ZeNHvhPfTCCe9FDKFummeAq+kIXs53m7x6bscQ3Lj\n" +
            "w05MyuiXWzN0W7N889l8qLuaEMKCsUfSaH/9tcw75HZBHsosePY8oAjvtXgUMPFx\n" +
            "obuwrgUCgYEAxPuozDr7F1tMRAwPtIlX5wLb8uVxXel4Svl2dRJJ9PyItMuTCMK3\n" +
            "c6QSbt9QJJP8P73gv7HVpLOYLvOBEq/OehaQ25FR6zDc7EaE1oj0q2N3lW7y8Nr9\n" +
            "GJLdDn//86i4KFxaf6nRU88QXJKz2NJT3k2WwsIE3o0lFmZga8YiIccCgYEA9E6t\n" +
            "XYDbJhuNBsSRBUhHzcOwERDirYIcaNsxxlBJBmdFKVTdo90byigd+kYEU0WpmTjS\n" +
            "Vf3uEAHsYkM/wS4kVlpd+v0zd0CoabzW1fnXTi/7wlOB4vrhx80vWHO22hHyrkTX\n" +
            "ezKq//AVBlmhjuFY+yqhHKVo4K/L3dNwqAYXCUMCgYBwLxL3HTAbITfSGTxoiT+y\n" +
            "pQI21001Ot3zdRdtnTjZeWkx7i6S8rIf/fUxh6TQ8Cbc9nqlMdaGsnGda7i6t71T\n" +
            "8r4VDjIlS/LF7XOB6wXNBhz40fMyEMXL1PhoZaWTUydudQplYoWAwZCD6FjcxwxU\n" +
            "ssOFr5GuXZwdobiQKIsPyQKBgAVO8FVnx1s8ngPXoY8L0wOVjO3SABrlCNj+akZ0\n" +
            "2CFbfRU40tgMpd3uoTge7Vkh2l2J7ogPzGxsnkZET85Swldd/0zE06lzrjUd9U0Q\n" +
            "8KyyPjYqulfEO8Oroau6V+7FFRDUThpPL4gAH3TT3b7NBrHuazHEJlM7fqKDRZ9h\n" +
            "An8hAoGBAKcAQkIfVTUUe/KEPEho7pvrLx1M2hNXuWB9v1j3fpsy91mggIysTFPP\n" +
            "6Pc/+LyS/DMhgoF0KmlUkdzdcUe9ELcWPmerap6mYEtcSyBfRq9whth46E0zE22u\n" +
            "JMYbxCd4QKDjU5Q+qj9OQjMqwqWguNGhapkT7/naOWqOl2SqpZpE\n" +
            "-----END RSA PRIVATE KEY-----";

    private static final String SERVER_CERT_KEY = "server-cert-key";

    @Test
    @DisplayName("Should load key store and trust store correctly")
    void initKeyStore(@TempDir Path tempDir) throws Exception {
        Files.write(tempDir.resolve("cert"), POD_CERT.getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("issuing_ca"), ISSUING_CA.getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("private_key"), PRIVATE_KEY.getBytes(StandardCharsets.UTF_8));

        KeyStoreHelper keyStoreHelper = KeyStoreHelper.newInstanceForKeyStore(tempDir.toString(), SERVER_CERT_KEY, "cert", "issuing_ca", "private_key");
        KeyStore keyStore = keyStoreHelper.buildKeyStore();

        Assertions.assertThat(keyStore.getCertificate(SERVER_CERT_KEY).toString()).contains("partners");
        Assertions.assertThat(keyStore.getCertificateChain(SERVER_CERT_KEY).length).isEqualTo(2);
        Assertions.assertThat(keyStore.getCertificateChain(SERVER_CERT_KEY)[0].toString()).contains("vault");
    }

    @Test
    @DisplayName("Should fail when certs are not present")
    void failWhenCertsNotPresent(@TempDir Path tempDir) {
        assertThrows(IllegalStateException.class, () -> KeyStoreHelper.newInstanceForKeyStore(tempDir.toString(), SERVER_CERT_KEY, "cert", "issuing_ca", "private_key"));
    }
}
