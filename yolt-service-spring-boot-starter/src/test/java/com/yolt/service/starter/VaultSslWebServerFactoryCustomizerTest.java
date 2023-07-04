package com.yolt.service.starter;

import com.yolt.service.starter.YoltWebServerVaultAutoConfiguration.VaultSslWebServerFactoryCustomizer;
import nl.ing.lovebird.vault.KeyStoreHelper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import static com.yolt.service.starter.YoltWebServerVaultAutoConfiguration.VaultSslWebServerFactoryCustomizer.SERVER_CERT_KEY_ALIAS;
import static com.yolt.service.starter.YoltWebServerVaultAutoConfigurationTest.ISSUING_CA;
import static com.yolt.service.starter.YoltWebServerVaultAutoConfigurationTest.POD_CERT;
import static com.yolt.service.starter.YoltWebServerVaultAutoConfigurationTest.PRIVATE_KEY;
import static nl.ing.lovebird.vault.KeyStoreHelper.KEY_STORE_PASSWORD;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class VaultSslWebServerFactoryCustomizerTest {

    @Test
    void shouldCreateKeystore(@TempDir Path tempDir) throws Exception {
        Files.write(tempDir.resolve("cert"), POD_CERT.getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("issuing_ca"), ISSUING_CA.getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("private_key"), PRIVATE_KEY.getBytes(StandardCharsets.UTF_8));

        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        VaultSslWebServerFactoryCustomizer customizer = new VaultSslWebServerFactoryCustomizer(tempDir.toString(), 8443, "cert", "issuing_ca", "private_key");
        customizer.customize(factory);

        KeyStore keyStore = factory.getSslStoreProvider().getKeyStore();

        assertAll(
                () -> assertThat(keyStore.getCertificate(SERVER_CERT_KEY_ALIAS).toString()).contains("partners"),
                () -> assertThat(keyStore.getCertificateChain(SERVER_CERT_KEY_ALIAS).length).isEqualTo(2),
                () -> assertThat(keyStore.getCertificateChain(SERVER_CERT_KEY_ALIAS)[0].toString()).contains("vault"),
                () -> assertThat(keyStore.getKey(SERVER_CERT_KEY_ALIAS, KEY_STORE_PASSWORD.toCharArray())).isNotNull()
        );
    }
}
