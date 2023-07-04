package nl.ing.lovebird.cassandra.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@ConfigurationProperties("yolt.vault.cassandra")
public class YoltVaultCassandraProperties {
    private Path vaultCredsFile = Paths.get("/vault/secrets/cassandra");
}

