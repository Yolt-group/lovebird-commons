package nl.ing.lovebird.kafka.autoconfigure;

import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.vault.KeyStoreHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class VaultKafkaKeystoreInitializer {

    public static final String KAFKA_CLIENT_CERT_KEY_ALIAS = "kafka-client-key";
    public static final String KEY_STORE_NAME = "kafka_key_store.jks";
    public static final String TRUST_STORE_NAME = "kafka_trust_store.jks";
    public static final String KAFKA_CERT_FILE_NAME = "kafka_cert";
    public static final String KAFKA_ISSUING_CA = "kafka_issuing_ca";
    public static final String KAFKA_PRIVATE_KEY = "kafka_private_key";

    private final String vaultSecretsDirectory;
    private final File keyStoreFile;
    private final File trustStoreFile;

    // See https://docs.oracle.com/cd/E14571_01/install.1111/e12002/oimscrn011.htm#BABJDAAA for the password policy
    private final char[] keyStorePw = "donotchange2".toCharArray();
    private final char[] trustStorePw = "trusted".toCharArray();

    public VaultKafkaKeystoreInitializer(String vaultSecretsDirectory) {
        this.vaultSecretsDirectory = Objects.requireNonNull(vaultSecretsDirectory);
        this.keyStoreFile = new File(vaultSecretsDirectory, KEY_STORE_NAME);
        this.trustStoreFile = new File(vaultSecretsDirectory, TRUST_STORE_NAME);
    }

    public void initializeKeyStore() {
        try (final FileOutputStream keyStoreFos = new FileOutputStream(keyStoreFile);
             final FileOutputStream trustKeyStoreFos = new FileOutputStream(trustStoreFile)) {
            KeyStoreHelper keyStoreHelper = KeyStoreHelper.newInstanceForKeyStore(
                    vaultSecretsDirectory, KAFKA_CLIENT_CERT_KEY_ALIAS, KAFKA_CERT_FILE_NAME, KAFKA_ISSUING_CA, KAFKA_PRIVATE_KEY
            );
            keyStoreHelper.buildKeyStore().store(keyStoreFos, keyStorePw);
            keyStoreHelper.buildTrustStore().store(trustKeyStoreFos, trustStorePw);
        } catch (Exception e) {
            final String msg = "Unable to create a key store based on the files located in: " + this.vaultSecretsDirectory;
            log.error(msg, e);
            throw new IllegalStateException(msg, e);
        }
    }

    /**
     * You can use this function to get the Kafka properties needed to work with SSL Client Certificates.
     * Properties have the same name for Kafka producer and consumer, so you can use this function for both.
     *
     * @return a Map of kafka properties which set up Kafka to use SSL client certs
     */
    public Map<String, Object> kafkaProperties() {
        final Map<String, Object> kafkaProperties = new HashMap<>();
        kafkaProperties.put("security.protocol", "SSL");
        kafkaProperties.put("ssl.truststore.location", trustStoreFile.toString());
        kafkaProperties.put("ssl.truststore.password", new String(this.trustStorePw));
        kafkaProperties.put("ssl.keystore.password", new String(this.keyStorePw));
        kafkaProperties.put("ssl.key.password", KeyStoreHelper.KEY_STORE_PASSWORD);
        kafkaProperties.put("ssl.keystore.location", keyStoreFile.toString());
        return kafkaProperties;
    }

}

