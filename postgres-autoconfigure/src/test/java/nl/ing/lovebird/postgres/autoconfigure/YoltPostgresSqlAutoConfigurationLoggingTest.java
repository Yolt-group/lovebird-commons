package nl.ing.lovebird.postgres.autoconfigure;

import nl.ing.lovebird.postgres.test.EnableExternalPostgresTestDatabase;
import nl.ing.lovebird.postgres.test.ExternalPostgresTestDatabaseContextCustomizerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.test.context.ContextCustomizer;
import org.springframework.test.context.ContextCustomizerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class YoltPostgresSqlAutoConfigurationLoggingTest {

    private static final String PRIMARY_KEY = "test_pk";
    private static final String CREATE_TABLE =
            "CREATE TABLE pg_temp.logging_test ("
                    + "  id text, "
                    + "  account bigint, "
                    + "  CONSTRAINT " + PRIMARY_KEY + " PRIMARY KEY (id)"
                    + ")";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(
                    // Used when running in CI. Can't work out how
                    //  POSTGRES_DB: integration-test-db
                    //  POSTGRES_USER: runner
                    //  POSTGRES_PASSWORD: runner
                    // are turned into a jdbc url
                    "spring.datasource.url=jdbc:postgresql://127.0.0.1:5432/integration-test-db",
                    "spring.datasource.username=runner",
                    "spring.datasource.password=runner",
                    "spring.datasource.driver-class-name=org.postgresql.Driver"
            )
            .withInitializer(applicationContext -> {
                @EnableExternalPostgresTestDatabase
                class ExampleTest {

                }
                ContextCustomizerFactory factory = new ExternalPostgresTestDatabaseContextCustomizerFactory();
                ContextCustomizer customizer = factory.createContextCustomizer(ExampleTest.class, Collections.emptyList());
                customizer.customizeContext(applicationContext, null);
            })
            .withConfiguration(AutoConfigurations.of(
                    DataSourceAutoConfiguration.class,
                    YoltPostgreSqlAutoConfiguration.class
            ));

    private static String generateInsertSQL(String userId, String account) {
        return "INSERT INTO pg_temp.logging_test (id, account) VALUES ('" + userId + "', '" + account + "')";
    }

    private static void execute(String sql, Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    @Test
    void shouldLogWhenLogErrorDetailIsOn() {
        contextRunner
                .withPropertyValues("spring.datasource.hikari.data-source-properties.logServerErrorDetail=true")
                .run(context -> {
                    DataSource dataSource = context.getBean(DataSource.class);
                    try (Connection connection = dataSource.getConnection()) {
                        execute(CREATE_TABLE, connection);
                        execute(generateInsertSQL("John Doe", "123456"), connection);
                        assertThatThrownBy(() -> execute(generateInsertSQL("John Doe", "123456"), connection))
                                .hasMessageContaining("Detail: Key (id)=(John Doe) already exists.")
                                .hasMessageContaining("ERROR: duplicate key value violates unique constraint \"test_pk\"");
                    }
                });
    }

    @Test
    void shouldNotLogWhenLogErrorDetailIsOff() {
        contextRunner
                .withPropertyValues("spring.datasource.hikari.data-source-properties.logServerErrorDetail=false")
                .run(context -> {
                    DataSource dataSource = context.getBean(DataSource.class);
                    try (Connection connection = dataSource.getConnection()) {
                        execute(CREATE_TABLE, connection);
                        execute(generateInsertSQL("John Doe", "123456"), connection);
                        assertThatThrownBy(() -> execute(generateInsertSQL("John Doe", "123456"), connection))
                                .hasMessageNotContaining("Detail: Key (id)=(John Doe) already exists.")
                                .hasMessageContaining("ERROR: duplicate key value violates unique constraint \"test_pk\"");
                    }
                });
    }

    @Test
    void shouldNotLogWhenLogErrorDetailIsOffByDefault() {
        contextRunner
                .run(context -> {
                    DataSource dataSource = context.getBean(DataSource.class);
                    try (Connection connection = dataSource.getConnection()) {
                        execute(CREATE_TABLE, connection);
                        execute(generateInsertSQL("John Doe", "123456"), connection);
                        assertThatThrownBy(() -> execute(generateInsertSQL("John Doe", "123456"), connection))
                                .hasMessageNotContaining("Detail: Key (id)=(John Doe) already exists.")
                                .hasMessageContaining("ERROR: duplicate key value violates unique constraint \"test_pk\"");
                    }
                });
    }
}
