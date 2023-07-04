package nl.ing.lovebird.postgres.autoconfigure;

import com.zaxxer.hikari.HikariDataSource;
import nl.ing.lovebird.postgres.autoconfigure.PostgreSqlAuthProvider.Authentication;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YoltPostgreSqlAutoConfigurationTest {
    private static final String TEST_ROLE = "test-application";
    private static final String USER_FROM_VAULT = "user_from_vault";
    private static final String PASSWORD_FROM_VAULT = "password_from_vault";
    private static final String USER_FROM_PROPERTIES = "user_from_properties";
    private static final String PASSWORD_FROM_PROPERTIES = "password_from_properties";

    private static final PostgreSqlAuthProvider CREDENTIALS_FROM_VAULT = () -> new Authentication(USER_FROM_VAULT, PASSWORD_FROM_VAULT);

    private static final AtomicReference<Properties> lastConnectionInfo = new AtomicReference<>();
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(YoltPostgreSqlAutoConfiguration.class))
            .withPropertyValues(
                    "spring.datasource.url=jdbc:postgresql://localhost:5432/",
                    "spring.datasource.driverClassName=" + MockDriver.class.getName(),
                    "spring.datasource.type=com.zaxxer.hikari.HikariDataSource",
                    "spring.datasource.user-name=" + USER_FROM_PROPERTIES,
                    "spring.datasource.password=" + PASSWORD_FROM_PROPERTIES,
                    "spring.application.name=" + TEST_ROLE
            );

    @Test
    void shouldCreateDataSourceIfVaultPostgreSQLSecretsRegistered() {
        contextRunner
                .withBean(PostgreSqlAuthProvider.class, () -> CREDENTIALS_FROM_VAULT)
                .run(context -> assertThat(context)
                        .hasSingleBean(YoltPostgreSqlAutoConfiguration.class)
                        .hasSingleBean(DataSource.class)
                );
    }

    @Test
    void shouldAllowOptionalExtraDataSources() {
        contextRunner
                .withBean("heavyDataSource", DataSource.class, () -> Mockito.mock(DataSource.class))
                .withBean(PostgreSqlAuthProvider.class, () -> CREDENTIALS_FROM_VAULT)
                .run(context -> {
                            assertThat(context).hasSingleBean(YoltPostgreSqlAutoConfiguration.class)
                                    .getBeanNames(DataSource.class)
                                    .containsExactlyInAnyOrder("dataSource", "heavyDataSource");
                        }
                );
    }

    @Test
    void shouldAllowDataSourceToBeRedefined() {
        contextRunner
                .withBean("dataSource", DataSource.class, () -> Mockito.mock(DataSource.class))
                .withBean(PostgreSqlAuthProvider.class, () -> CREDENTIALS_FROM_VAULT)
                .run(context -> {
                            assertThat(context).hasSingleBean(YoltPostgreSqlAutoConfiguration.class)
                                    .getBeanNames(DataSource.class)
                                    .containsExactlyInAnyOrder("dataSource");
                        }
                );
    }

    @Test
    void shouldCreateDataSourceIfVaultPostgreSQLSecretsMissing() {
        contextRunner
                .run(context -> assertThat(context)
                        .hasSingleBean(YoltPostgreSqlAutoConfiguration.class)
                        .hasSingleBean(DataSource.class)
                );
    }

    @Test
    void shouldPickUpConfigurationProperties() {
        contextRunner
                .withPropertyValues(
                        "spring.datasource.hikari.minimum-idle=42",
                        "spring.datasource.hikari.data-source-properties.logServerErrorDetail=true",
                        "spring.datasource.hikari.data-source-properties.sslmode=verify-ca"
                )
                .run(context -> {
                            DataSource dataSource = context.getBean(DataSource.class);
                            HikariDataSource hikariDataSource = dataSource.unwrap(HikariDataSource.class);
                            assertThat(hikariDataSource.getMinimumIdle()).isEqualTo(42);
                            assertThat(hikariDataSource.getDataSourceProperties().get("logServerErrorDetail")).isEqualTo("true");
                            assertThat(hikariDataSource.getDataSourceProperties().get("sslmode")).isEqualTo("verify-ca");
                        }
                );
    }

    @Test
    void shouldSetRoleUsernamePasswordForDataSourceWhenVaultPostgresSecretsAvailable() {
        contextRunner
                .withBean(PostgreSqlAuthProvider.class, () -> CREDENTIALS_FROM_VAULT)
                .withPropertyValues("yolt.vault.enabled=true")
                .run(context -> {
                            DataSource dataSource = context.getBean(DataSource.class);
                            HikariDataSource hikariDataSource = dataSource.unwrap(HikariDataSource.class);
                            assertThat(hikariDataSource.getConnectionInitSql()).isEqualTo("SET ROLE '" + TEST_ROLE + "'");
                            assertThat(hikariDataSource.getUsername()).isEqualTo(USER_FROM_VAULT);
                            assertThat(hikariDataSource.getPassword()).isEqualTo(PASSWORD_FROM_VAULT);
                        }
                );
    }

    @Test
    void shouldSetRoleUsernamePasswordForDataSourceWhenVaultPostgresSecretsUnavailable() {
        contextRunner
                .run(context -> {
                            DataSource dataSource = context.getBean(DataSource.class);
                            HikariDataSource hikariDataSource = dataSource.unwrap(HikariDataSource.class);
                            assertThat(hikariDataSource.getConnectionInitSql()).isNull();
                            assertThat(hikariDataSource.getUsername()).isEqualTo(USER_FROM_PROPERTIES);
                            assertThat(hikariDataSource.getPassword()).isEqualTo(PASSWORD_FROM_PROPERTIES);
                        }
                );
    }

    @Test
    void shouldNotSetRoleWhenCustomConnectionInitSqlConfigurerBeanIsDefined() {
        contextRunner
                .withBean(ConnectionInitSqlConfigurer.class, () -> dataSource -> {
                })
                .run(context -> {
                            DataSource dataSource = context.getBean(DataSource.class);
                            HikariDataSource hikariDataSource = dataSource.unwrap(HikariDataSource.class);
                            assertThat(hikariDataSource.getConnectionInitSql()).isNull();
                        }
                );
    }

    @Test
    void shouldRejectInvalidApplicationNamesAsRole() {

        YoltPostgreSqlAutoConfiguration configuration = new YoltPostgreSqlAutoConfiguration();
        assertThrows(
                IllegalArgumentException.class,
                () -> configuration.useApplicationNameAsRole("InvalidRole!@#$%^&*()_+=<>")
        );
    }

    @Test
    void shouldChangeUserNameAndPasswordUseByConnectionIfVaultChangesCredentials() {
        AtomicReference<Authentication> authenticationAtomicReference =
                new AtomicReference<>(new Authentication("user1", "pass1"));

        contextRunner
                .withBean(PostgreSqlAuthProvider.class, () -> authenticationAtomicReference::get)
                .run(context -> {
                            DataSource dataSource = context.getBean(DataSource.class);
                            HikariDataSource hikariDataSource = dataSource.unwrap(HikariDataSource.class);

                            assertDoesNotThrow(() -> hikariDataSource.getConnection());
                            Properties properties = new Properties();
                            properties.setProperty("user", "user1");
                            properties.setProperty("password", "pass1");
                            properties.setProperty("logServerErrorDetail", "false");
                            assertThat(lastConnectionInfo).hasValue(properties);

                            authenticationAtomicReference.set(new Authentication("user2", "pass2"));
                            assertDoesNotThrow(() -> hikariDataSource.getConnection());
                            Properties properties2 = new Properties();
                            properties2.setProperty("user", "user2");
                            properties2.setProperty("password", "pass2");
                            properties2.setProperty("logServerErrorDetail", "false");
                            assertThat(lastConnectionInfo).hasValue(properties2);
                        }
                );
    }

    public static class MockDriver implements Driver {

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            lastConnectionInfo.set(info);
            Connection mock = Mockito.mock(Connection.class);
            Mockito.when(mock.createStatement()).thenReturn(Mockito.mock(Statement.class));
            return mock;
        }

        @Override
        public boolean acceptsURL(String url) {
            return true;
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) {
            return new DriverPropertyInfo[0];
        }

        @Override
        public int getMajorVersion() {
            return 0;
        }

        @Override
        public int getMinorVersion() {
            return 0;
        }

        @Override
        public boolean jdbcCompliant() {
            return false;
        }

        @Override
        public Logger getParentLogger() {
            return null;
        }
    }

}
