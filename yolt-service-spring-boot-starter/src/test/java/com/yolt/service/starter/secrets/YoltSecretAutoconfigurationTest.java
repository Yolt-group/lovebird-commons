package com.yolt.service.starter.secrets;

import com.yolt.securityutils.crypto.CryptoUtils;
import nl.ing.lovebird.secretspipeline.PGPKeyRing;
import nl.ing.lovebird.secretspipeline.VaultKeys;
import org.apache.tomcat.util.codec.binary.Base64;
import org.assertj.core.util.Lists;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.c02e.jpgpj.Ring;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.Security;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.ing.lovebird.secretspipeline.converters.KeyStoreReader.TYPE_INDICATOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class YoltSecretAutoconfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(YoltSecretsAutoConfiguration.class));

    @Test
    @DisplayName("[SHOULD NOT] contain bean [GIVEN] enabled flag is set to false")
    void notEnabledShouldNotHaveBeanInContext() {
        contextRunner
                .withPropertyValues("yolt.vault.secret.enabled=false")
                .run(c -> assertThat(c)
                        .doesNotHaveBean(YoltSecretsAutoConfiguration.class));
    }

    @Test
    @DisplayName("[SHOULD NOT] contain bean [GIVEN] enabled flag is set to true")
    void enabledShouldHaveBeanInContext() {
        contextRunner
                .withPropertyValues("yolt.vault.secret.enabled=true")
                .run(c -> assertThat(c)
                        .hasSingleBean(YoltSecretsAutoConfiguration.class));
    }

    @Test
    @DisplayName("[SHOULD] get KeyRing and Keystore [GIVEN] BC provider name")
    void canGetKeyRingAndKeystoreWithProvider(@TempDir Path folder) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        assertNotNull(Security.getProvider(BouncyCastleProvider.PROVIDER_NAME));
        testActualKeyGeneration(folder);
    }

    @Test
    @DisplayName("[SHOULD] get KeyRing and Keystore [GIVEN] no provider name")
    void canGetKeyRingAndKeystoreWithNoProvider(@TempDir Path folder) throws IOException {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        assertNull(Security.getProvider(BouncyCastleProvider.PROVIDER_NAME));
        testActualKeyGeneration(folder);
    }

    private void testActualKeyGeneration(Path folder) throws IOException {
        prepareSecretFiles(folder);
        contextRunner
                .withPropertyValues("yolt.vault.secret.enabled=true",
                        // Override default location in YoltVaultConfiguration
                        "yolt.vault.secret.location=" + folder.toUri())
                .run(c -> {
                    VaultKeys vaultKeys = c.getBean(VaultKeys.class);
                    PGPKeyRing keyRing = c.getBean(PGPKeyRing.class);
                    Ring ring = keyRing.getRing();
                    assertThat(ring.getKeys().size()).isEqualTo(1);
                    assertThat(vaultKeys.getSymmetricKey("test").keySizeInBits()).isEqualTo(256);
                });
    }

    private void prepareSecretFiles(Path folder) throws IOException {
        String pgpKey = "-----BEGIN PGP PRIVATE KEY BLOCK-----\n" +
                "\n" +
                "lQPGBFyKMyEBCACwIgX6oYBxHasN6irUhxY0QdO+veUQmElH07WqwLhCRNo6mLBX\n" +
                "Oi/OBIBBUN/w36LyR0YVovk2fCfA2WaeHHkLeDKo1HoHIz7ByrV03Jlp7aQkV3G2\n" +
                "z9D2fEGmFnbJ2mclh7bDKsQPJkjL2dsuYrlCzNUCw3m1Jf2HhW1q6JkU2JpHfmW+\n" +
                "ho+9ZIBWn1F4yJXYot5ziSvDjiJjcLTK26unNfB7C50wdwpSz5IlV6h7pW4XL44d\n" +
                "a/q5QeUp8Swgu736m0ahgwkX2DpxwoZSBGGVBYaElE1W157H/Q8sDi3BBvEe49mZ\n" +
                "j4+F1mNw3ijmdAAHreu7jdC2SVlDbiSsR6stABEBAAH+BwMCloTWoxEIK2Xpwz+v\n" +
                "ztCf7YPmIj0S0bcEvox24a+MdUj41DdNoqNMvFFybEedeOgTjzHNiDTPVWDTTAjh\n" +
                "131iilL54oe+YsO7DhcXiTGT2GhPZoGonl4b7Vmotx50WsnO6GfNivZoLubsUhDA\n" +
                "45SfoVpWrbJdakW/Cqz8d2LtxtXfKt8LPk84pfE5S2PHAyVF+1mOciXHfhxQ6Cpj\n" +
                "9yuQ5Ymf249TaCqBMgy9oZkFCGn49gowDxc4F6mtPNy+YZ6LtLUauYZkSnjPu2C6\n" +
                "lor9tbSzIhjMWux67UjrDvdzkbzqS/PRUQHjP/CXD9l13ZJ2Fzbu7xH2XsN5AXpP\n" +
                "ctWF32b+E7KPJGXztZSjsb8FL7eGQhK8s9CgULhYbN095Xo9Aa9eYISZhyJDV4z5\n" +
                "3QZMhJHwUT+ReH1PW/tizklAYt5hDUksX7aDH5gk5us1uQOdb01abEAOkNZYvb+l\n" +
                "KnqqXK51y+bP/H9mumyN4jV82QRQKSq6Nt5e8g1qQyisvxPTBK9Bbr0XFTfT7Zxq\n" +
                "KOK3zaqh/PQMRs9tkoi+/gTWAaCAxAuGGEleZTNfEmSjejAezxHnTwVPFiAEUD5M\n" +
                "5p+TtuLh9QYBYnV/qYmGXMt+b++v1NCFxJaaiqDARw3yBg7axsinD+RnQmE4xFMK\n" +
                "X5xeKbKbVUThsjMJvhHDYdFLpd6W4rz3SxzUCC4F0uczqKuksgXh+oQWwy2PA8LE\n" +
                "hOo6ELYjUm43MDmJdqlQQlUvFueszhJ3q9iNSmuLoBXmjPRaXxFY531nYNM8Dorz\n" +
                "E6cS3E1HUFBUNkhqNRQRxYGiLGOjq8RV4dzsg4ybdRTrF+XDCc9fZozd5b3bauye\n" +
                "a7UXc70nQPoxHSR2K2UaZSx2DrO2B7R81T8Cqf7D9yOXFB2Mh4mAx94GKvddfCe0\n" +
                "FIs2y10whwiDtBt5b2x0LWt5Yy1kZXYgPGRldkB5b2x0LmNvbT6JAU4EEwEIADgW\n" +
                "IQRRdsM6LZM762bbsEL19wy1pogHpwUCXIozIQIbDwULCQgHAgYVCgkICwIEFgID\n" +
                "AQIeAQIXgAAKCRD19wy1pogHpyWqCACERD3PbEMVDHSCaIymGqiQ3/nI5i/GvQL2\n" +
                "PMwCxs0DEyFni0++QMKwnrhdC9BZh27JbExQmcgIOLS0THfIjXZlUb4uE9dnseXJ\n" +
                "6/A01BhOhbRSXkjTv9Iv7pGn0Zsdl7EQ3NJsYl6lqRa3bWAfMfx4zDvqIeHDYOqT\n" +
                "DOtO2C8RRUh8u9lSBnSMgAcNiO2BqmWrR4sdyU5vGfV8eltl8KwVhlo8NwUUZAzm\n" +
                "ZSvQMGD4GbUJf3i6TmX82z2mWAJtAWeDrKkfSfj7bX67j7asmId8VSauYniSd7QG\n" +
                "xEqOIL6a0WK3DRkWNb42qIDc0ws0VhNbq8OVOTVYIG3XripIUnun\n" +
                "=wlog\n" +
                "-----END PGP PRIVATE KEY BLOCK-----";

        Path pgpKeyFile = folder.resolve("symmetric_GPG");
        Files.write(pgpKeyFile, Lists.newArrayList(TYPE_INDICATOR + "GPG"));
        Files.write(pgpKeyFile, Base64.encodeBase64(pgpKey.getBytes(UTF_8)), StandardOpenOption.APPEND);

        byte[] encodedSymmetricKey = Base64.encodeBase64(CryptoUtils.getRandomKey());
        Path encodedSymmetricKeyFile = folder.resolve("test");
        Files.write(encodedSymmetricKeyFile, Lists.newArrayList(TYPE_INDICATOR + "key_256"));
        Files.write(encodedSymmetricKeyFile, encodedSymmetricKey, StandardOpenOption.APPEND);
    }
}
