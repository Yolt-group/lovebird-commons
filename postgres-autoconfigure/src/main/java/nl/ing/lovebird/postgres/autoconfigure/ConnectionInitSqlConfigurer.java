package nl.ing.lovebird.postgres.autoconfigure;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Specialization of the {@code DataSourceConfigurer} to configure
 * the connection init sql string.
 */
public interface ConnectionInitSqlConfigurer extends DataSourceConfigurer {

    void configure(HikariDataSource dataSource);

}
