package nl.ing.lovebird.postgres.autoconfigure;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public interface PostgreSqlAuthProvider {

    Authentication newAuthentication();

    @Getter
    @RequiredArgsConstructor
    class Authentication {
        final String username;
        final String password;
    }
}
