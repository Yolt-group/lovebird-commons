package nl.ing.lovebird.kafka.autoconfigure;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import static nl.ing.lovebird.kafka.autoconfigure.TestCertificates.*;
import static nl.ing.lovebird.kafka.autoconfigure.VaultKafkaKeystoreInitializer.KEY_STORE_NAME;
import static nl.ing.lovebird.kafka.autoconfigure.VaultKafkaKeystoreInitializer.TRUST_STORE_NAME;

class VaultKafkaKeystoreInitializerTest {

    @Test
    @DisplayName("Should store both key store and trust store on specified path")
    void shouldStoreKeyStores(@TempDir Path tempDir) throws Exception {
        Files.write(tempDir.resolve("kafka_cert"), POD_CERT.getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("kafka_issuing_ca"), ISSUING_CA.getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("kafka_private_key"), PRIVATE_KEY.getBytes(StandardCharsets.UTF_8));

        VaultKafkaKeystoreInitializer keystoreInitializerVaultAgent = new VaultKafkaKeystoreInitializer(tempDir.toString());
        keystoreInitializerVaultAgent.initializeKeyStore();

        Assertions.assertTrue(tempDir.resolve(KEY_STORE_NAME).toFile().exists());
        Assertions.assertTrue(tempDir.resolve(TRUST_STORE_NAME).toFile().exists());
    }

    @Test
    @DisplayName("Load key store with wrong password should fail")
    void failWithWrongPassword(@TempDir Path tempDir) throws Exception {
        Files.write(tempDir.resolve("kafka_cert"), POD_CERT.getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("kafka_issuing_ca"), ISSUING_CA.getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("kafka_private_key"), PRIVATE_KEY.getBytes(StandardCharsets.UTF_8));
        Path keyStorePath = tempDir.resolve(KEY_STORE_NAME);

        VaultKafkaKeystoreInitializer keystoreInitializerVaultAgent = new VaultKafkaKeystoreInitializer(tempDir.toString());
        keystoreInitializerVaultAgent.initializeKeyStore();

        Assertions.assertTrue(keyStorePath.toFile().exists());
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try(FileInputStream in = new FileInputStream(keyStorePath.toFile())) {
            Assertions.assertThrows(IOException.class, () -> keyStore.load(in, "wrong".toCharArray()));
        }
    }

    @Test
    @DisplayName("Load key store with correct password should work")
    void loadWithCorrectPassword(@TempDir Path tempDir) throws Exception {
        Files.write(tempDir.resolve("kafka_cert"), POD_CERT.getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("kafka_issuing_ca"), ISSUING_CA.getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("kafka_private_key"), PRIVATE_KEY.getBytes(StandardCharsets.UTF_8));
        Path keyStorePath = tempDir.resolve(KEY_STORE_NAME);

        VaultKafkaKeystoreInitializer keystoreInitializerVaultAgent = new VaultKafkaKeystoreInitializer(tempDir.toString());
        keystoreInitializerVaultAgent.initializeKeyStore();

        Assertions.assertTrue(keyStorePath.toFile().exists());
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try(FileInputStream in = new FileInputStream(keyStorePath.toFile())) {
            Assertions.assertDoesNotThrow(() -> keyStore.load(in, "donotchange2".toCharArray()));
        }
    }


    @Test
    @DisplayName("Should overwrite existing key store")
    void overwrite(@TempDir Path tempDir) throws Exception {
        Files.write(tempDir.resolve("kafka_cert"), POD_CERT.getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("kafka_issuing_ca"), ISSUING_CA.getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("kafka_private_key"), PRIVATE_KEY.getBytes(StandardCharsets.UTF_8));
        Path keyStorePath = tempDir.resolve(KEY_STORE_NAME);

        VaultKafkaKeystoreInitializer keystoreInitializerVaultAgent = new VaultKafkaKeystoreInitializer(tempDir.toString());
        keystoreInitializerVaultAgent.initializeKeyStore();
        keystoreInitializerVaultAgent.initializeKeyStore();

        Assertions.assertTrue(keyStorePath.toFile().exists());
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (FileInputStream in = new FileInputStream(keyStorePath.toFile()) ){
            Assertions.assertDoesNotThrow(() ->keyStore.load(in, "donotchange2".toCharArray()));
        }
    }

}