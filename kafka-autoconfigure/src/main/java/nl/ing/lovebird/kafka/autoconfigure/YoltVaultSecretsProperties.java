package nl.ing.lovebird.kafka.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("yolt.vault.secrets")
public class YoltVaultSecretsProperties {
    private String directory = "/vault/secrets/";
}

