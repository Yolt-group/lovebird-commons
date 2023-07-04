package nl.ing.lovebird.errorhandling.exception;

import org.springframework.web.client.RestClientException;

import java.util.UUID;

public class UserNotFoundException extends RestClientException {
    public UserNotFoundException() {
        super("User not found");
    }

    public UserNotFoundException(final UUID userId) {
        super(String.format("User data not found for userId %s", userId.toString()));
    }
}
