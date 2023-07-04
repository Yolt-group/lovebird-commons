package nl.ing.lovebird.postgres.autoconfigure;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * A datasource that will update connections credentials before use.
 * <p>
 * During regular operations vault may roll over the credentials used to
 * connect to postgress. This results in the connection failing. Spring will
 * then create a new connection and try again. By updating the credentials
 * before a new connection is created we ensure that the retry will succeed.
 */
public final class AuthAwareDataSource extends HikariDataSource {

    private PostgreSqlAuthProvider authProvider;

    public void setAuthProvider(PostgreSqlAuthProvider authProvider) {
        this.authProvider = authProvider;
        PostgreSqlAuthProvider.Authentication authentication = authProvider.newAuthentication();
        this.setUsername(authentication.getUsername());
        this.setPassword(authentication.getPassword());
    }

    @Override
    public Connection getConnection() throws SQLException {
        PostgreSqlAuthProvider.Authentication authentication = authProvider.newAuthentication();
        this.setUsername(authentication.getUsername());
        this.setPassword(authentication.getPassword());
        return super.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        PostgreSqlAuthProvider.Authentication authentication = authProvider.newAuthentication();
        this.setUsername(authentication.getUsername());
        this.setPassword(authentication.getPassword());
        return super.getConnection();
    }
}
