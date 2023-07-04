package com.yolt.service.starter.secrets;

import com.yolt.service.starter.vault.YoltVaultProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.secretspipeline.KeyRingCreator;
import nl.ing.lovebird.secretspipeline.PGPKeyRing;
import nl.ing.lovebird.secretspipeline.VaultKeys;
import nl.ing.lovebird.secretspipeline.VaultKeysReader;
import nl.ing.lovebird.secretspipeline.converters.CSRKeyStoreReader;
import nl.ing.lovebird.secretspipeline.converters.CertificateKeyStoreReader;
import nl.ing.lovebird.secretspipeline.converters.JsonWebKeysStoreReader;
import nl.ing.lovebird.secretspipeline.converters.KeyStoreReader;
import nl.ing.lovebird.secretspipeline.converters.PasswordKeyStoreReader;
import nl.ing.lovebird.secretspipeline.converters.PrivateKeyStoreReader;
import nl.ing.lovebird.secretspipeline.converters.SymmetricKeyStoreReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

@ConditionalOnProperty(name = "yolt.vault.secret.enabled", havingValue = "true")
@AutoConfiguration
@EnableConfigurationProperties(YoltVaultProperties.class)
@RequiredArgsConstructor
@Slf4j
public class YoltSecretsAutoConfiguration {

    private final YoltVaultProperties vaultProperties;

    @Bean
    @Qualifier("yolt-secrets-ring")
    @ConditionalOnMissingBean
    public PGPKeyRing getPGPKeyRing() throws IOException {
        Resource secretLocation = vaultProperties.getSecret().getLocation();
        if (secretLocation.exists()) {
            return new KeyRingCreator(secretLocation.getURI()).createKeyRing();
        } else {
            log.info("Location: {} does not exist, returning empty PGPKeyRing", secretLocation);
            return new PGPKeyRing();
        }
    }

    @Bean
    @Qualifier("yolt-secrets-keystore")
    @ConditionalOnMissingBean
    public VaultKeys getVaultKeys(List<KeyStoreReader> keyConverters) {
        try {
            Resource secretLocation = vaultProperties.getSecret().getLocation();
            if (secretLocation.exists()) {
                return new VaultKeysReader(keyConverters).readFiles(secretLocation.getURI());
            } else {
                log.info("Location: {} does not exist, returning empty VaultKeys", secretLocation);
                return new VaultKeys();
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Bean
    public KeyStoreReader certificateKeyStoreReader() {
        return new CertificateKeyStoreReader();
    }

    @Bean
    public KeyStoreReader passwordKeyStoreReader() {
        return new PasswordKeyStoreReader();
    }

    @Bean
    public KeyStoreReader privateKeyStoreReader() {
        return new PrivateKeyStoreReader();
    }

    @Bean
    public KeyStoreReader symmetricKeyStoreReader() {
        return new SymmetricKeyStoreReader();
    }

    @Bean
    public KeyStoreReader csrKeysStoreReader() {
        return new CSRKeyStoreReader();
    }

    @Bean
    public KeyStoreReader jsonWebKeysStoreReader() {
        return new JsonWebKeysStoreReader();
    }
}
