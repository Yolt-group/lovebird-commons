package nl.ing.lovebird.postgres.autoconfigure;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Configures the data source before it is used.
 */
public interface DataSourceConfigurer {

    void configure(HikariDataSource dataSource);

}
