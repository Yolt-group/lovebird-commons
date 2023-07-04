package com.yolt.service.starter;

import com.yolt.service.starter.vault.YoltVaultProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.vault.KeyStoreHelper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.SslStoreProvider;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;

import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.Security;

import static nl.ing.lovebird.vault.Vault.requireFileProvidedByVault;
import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

/*
 * Note: Since Spring Boot 2.7 we can use PEM files directly. So this class is
 * obsolete, in theory.
 *
 * In practice this means cleaning up the `server.ssl` properties everywhere
 * first. And also the `management.server.ssl` properties. Unfortunately
 * the management server can't be customized with a WebServerFactoryCustomizer
 * yet. So we're kinda stuck here.
 *
 * https://yolt.atlassian.net/browse/CHAP-160
 */
@ConditionalOnProperty(name = "yolt.vault.enabled", havingValue = "true")
@AutoConfiguration
@EnableConfigurationProperties(YoltVaultProperties.class)
@RequiredArgsConstructor
@Slf4j
public class YoltWebServerVaultAutoConfiguration {

    private final YoltVaultProperties vaultProperties;

    @Bean
    @ConditionalOnProperty(name = "yolt.vault.https.enabled", havingValue = "true")
    @ConditionalOnWebApplication(type = SERVLET)
    public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> vaultSSLWebServerFactoryCustomizer(
            @Value("${yolt.server.secure-port:8443}") final int securePort
    ) {
        String certFilename = vaultProperties.getSecrets().getTls().getCertFileName();
        String issuingCaFilename = vaultProperties.getSecrets().getTls().getIssuingCaFileName();
        String privateKeyFilename = vaultProperties.getSecrets().getTls().getPrivateKeyFileName();
        String vaultSecretsDirectory = vaultProperties.getSecrets().getDirectory();

        requireFileProvidedByVault(Paths.get(vaultSecretsDirectory, certFilename));
        requireFileProvidedByVault(Paths.get(vaultSecretsDirectory, issuingCaFilename));
        requireFileProvidedByVault(Paths.get(vaultSecretsDirectory, privateKeyFilename));

        log.info("Wiring SSL with vault-agent");
        return new VaultSslWebServerFactoryCustomizer(vaultSecretsDirectory, securePort, certFilename, issuingCaFilename,
                privateKeyFilename);
    }

    public static class VaultSslWebServerFactoryCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

        static final String SERVER_CERT_KEY_ALIAS = "server-cert-key";
        private final String vaultSecretsDirectory;
        private final int securePort;
        private final String certFilename;
        private final String issuingCaFilename;
        private final String privateKeyFilename;

        static {
            Security.addProvider(new BouncyCastleProvider());
        }

        public VaultSslWebServerFactoryCustomizer(String vaultSecretsDirectory, int securePort, String certFilename, String issuingCaFilename, String privateKeyFilename) {
            this.vaultSecretsDirectory = vaultSecretsDirectory;
            this.securePort = securePort;
            this.certFilename = certFilename;
            this.issuingCaFilename = issuingCaFilename;
            this.privateKeyFilename = privateKeyFilename;
        }

        /**
         * Simplified and extended from
         * https://github.com/jandd/spring-boot-vault-demo/blob/master/src/main/java/vaultdemo/SslCustomizationBean.java
         */
        @Override
        public void customize(final ConfigurableServletWebServerFactory factory) {
            KeyStoreHelper helper = KeyStoreHelper.newInstanceForKeyStore(vaultSecretsDirectory, SERVER_CERT_KEY_ALIAS, certFilename, issuingCaFilename, privateKeyFilename);
            factory.setSslStoreProvider(new SslStoreProvider() {
                @Override
                public KeyStore getKeyStore() {
                    return helper.buildKeyStore();
                }

                @Override
                public KeyStore getTrustStore() {
                    return helper.buildTrustStore();
                }
            });
            final Ssl ssl = new Ssl();
            ssl.setKeyStorePassword(KeyStoreHelper.KEY_STORE_PASSWORD);
            ssl.setKeyAlias(SERVER_CERT_KEY_ALIAS);
            factory.setSsl(ssl);
            factory.setPort(securePort);
        }
    }
}
