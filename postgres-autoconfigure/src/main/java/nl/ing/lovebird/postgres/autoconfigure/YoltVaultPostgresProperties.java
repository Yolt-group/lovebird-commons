package nl.ing.lovebird.postgres.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@ConfigurationProperties("yolt.vault.postgres-sql")
public class YoltVaultPostgresProperties {
    private Path vaultCredsFile = Paths.get("/vault/secrets/rds");
}

