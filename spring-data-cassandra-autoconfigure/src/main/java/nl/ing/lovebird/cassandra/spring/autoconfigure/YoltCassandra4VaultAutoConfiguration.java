package nl.ing.lovebird.cassandra.spring.autoconfigure;

import com.datastax.oss.driver.api.core.auth.AuthProvider;
import com.datastax.oss.driver.api.core.auth.PlainTextAuthProviderBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static nl.ing.lovebird.vault.Vault.requireFileProvidedByVault;

@Slf4j
@AutoConfiguration
@ConditionalOnClass({PlainTextAuthProviderBase.class})
@EnableConfigurationProperties({YoltVaultCassandraProperties.class})
@ConditionalOnProperty(value = "yolt.vault.enabled", havingValue = "true")
public class YoltCassandra4VaultAutoConfiguration {

    private final YoltVaultCassandraProperties vaultProperties;

    public YoltCassandra4VaultAutoConfiguration(YoltVaultCassandraProperties vaultProperties) {
        this.vaultProperties = vaultProperties;
    }

    @Bean
    public CqlSessionBuilderCustomizer cassandra4AuthProviderVault(AuthProvider authProvider) {
        return builder -> builder.withAuthProvider(authProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthProvider cassandra4VaultCredentialsAuthProvider() {
        log.info("Using Cassandra credentials provided by vault-injector");
        requireFileProvidedByVault(vaultProperties.getVaultCredsFile());
        return new YoltCassandra4VaultCredentialsAuthProvider(vaultProperties.getVaultCredsFile());
    }
}
