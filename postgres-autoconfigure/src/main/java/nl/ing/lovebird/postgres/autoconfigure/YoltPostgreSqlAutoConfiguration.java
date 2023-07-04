package nl.ing.lovebird.postgres.autoconfigure;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.vault.YoltVaultCredentialsReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.Properties;

import static nl.ing.lovebird.vault.Vault.requireFileProvidedByVault;

/**
 * This DataSource uses HikariDataSource and sets a role (the role name will match the service name) for each
 * connection to PostgreSQL. This is necessary to use PostgresSQL in our environment.
 * <p>
 * If you do not set the role before creating tables in the database, the tables will be created with
 * the wrong owner.
 * <p>
 * Setting the role is enabled by default, but if you do not need to set the role, the DataSource
 * can be disabled using the {@code yolt.postgres.set-role=false} property
 *
 * @see org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
 */
@ConditionalOnClass({DataSource.class, HikariDataSource.class})
@AutoConfiguration
@EnableConfigurationProperties({DataSourceProperties.class, YoltVaultPostgresProperties.class})
@RequiredArgsConstructor
@Slf4j
public class YoltPostgreSqlAutoConfiguration {

    public static final String DATA_SOURCE_BEAN_NAME = "dataSource";

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "yolt.vault.enabled", havingValue = "true")
    public PostgreSqlAuthProvider vaultPostgreSqlCredentials(YoltVaultPostgresProperties vaultProperties) {
        log.info("Using PostgreSQL credentials provided by vault-injector");
        requireFileProvidedByVault(vaultProperties.getVaultCredsFile());
        return new VaultPostgresSqlCredentialsAuthProvider(vaultProperties.getVaultCredsFile());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "yolt.vault.enabled", havingValue = "false", matchIfMissing = true)
    public PostgreSqlAuthProvider propertiesPostgreSqlCredentials(final DataSourceProperties properties) {
        log.info("Using PostgreSQL credentials provided by properties");
        return () -> new PostgreSqlAuthProvider.Authentication(properties.getUsername(), properties.getPassword());
    }

    /**
     * Vault will provide us with a random user name in the form application-name-random-string. By default this is also
     * the role used by the postgres connection. However this should be the application-name so we have to switch roles
     * when initializing the sql connection.
     */
    @Bean
    @ConditionalOnMissingBean(ConnectionInitSqlConfigurer.class)
    @ConditionalOnProperty(value = "yolt.vault.enabled", havingValue = "true")
    public ConnectionInitSqlConfigurer useApplicationNameAsRole(@Value("${spring.application.name}") String applicationName) {
        if (applicationName.matches(".*[^a-z-].*")) {
            // Prevents sql injection
            throw new IllegalArgumentException("spring.application.name='" + applicationName + "' may only contain a-z and '-'");
        }
        return dataSource -> {
            log.info("Setting role {} for dataSource", applicationName);
            dataSource.setConnectionInitSql(String.format("SET ROLE '%s'", applicationName));
        };
    }

    @Bean
    public DataSourceConfigurer doNotLogServerErrorDetailsByDefault() {
        return dataSource -> {
            // Set spring.datasource.hikari.data-source-properties.logServerErrorDetail=false if not
            // set otherwise.
            if (dataSource.getDataSourceProperties().getProperty("logServerErrorDetail") == null) {
                dataSource.addDataSourceProperty("logServerErrorDetail", false);
            }
        };
    }

    @Bean
    public DataSourceConfigurer propertyAsPoolName(DataSourceProperties properties) {
        return dataSource -> {
            if (StringUtils.hasText(properties.getName())) {
                dataSource.setPoolName(properties.getName());
            }
        };
    }

    @Bean
    public DataSourceConfigurer authProvider(PostgreSqlAuthProvider authProvider) {
        return dataSource -> {
            if (dataSource instanceof AuthAwareDataSource) {
                AuthAwareDataSource authAwareDataSource = (AuthAwareDataSource) dataSource;
                authAwareDataSource.setAuthProvider(authProvider);
            }
        };
    }

    @Bean(name = DATA_SOURCE_BEAN_NAME)
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    @ConditionalOnMissingBean(name = DATA_SOURCE_BEAN_NAME)
    public DataSource dataSource(final DataSourceProperties properties,
                                 final Collection<DataSourceConfigurer> dataSourceConfigurers) {
        // See DataSourceAutoConfiguration.Hikari
        AuthAwareDataSource dataSource = properties.initializeDataSourceBuilder()
                .type(AuthAwareDataSource.class)
                .build();
        dataSourceConfigurers.forEach(dataSourceConfigurer -> dataSourceConfigurer.configure(dataSource));
        return dataSource;
    }

    @RequiredArgsConstructor
    private static class VaultPostgresSqlCredentialsAuthProvider implements PostgreSqlAuthProvider {
        private final Path vaultCredentialFile;
        private String username;

        @Override
        public Authentication newAuthentication() {
            final Properties credentials = YoltVaultCredentialsReader.readCredentials(vaultCredentialFile);
            String newUsername = (String) credentials.get("username");
            if (!Objects.equals(username, newUsername)) {
                log.info("Vault-injector provided a new username");
                username = newUsername;
            }
            String password = (String) credentials.get("password");
            return new Authentication(newUsername, password);
        }
    }

}
