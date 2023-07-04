package nl.ing.lovebird.clienttokens.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.errorhandling.ErrorInfo;

@RequiredArgsConstructor
@Getter
public enum ErrorConstants implements ErrorInfo {

    INVALID_CLIENT_TOKEN("9001", "The client-token is invalid."),
    UNAUTHORIZED_CLIENT_TOKEN_REQUESTER("9002", "Token requester for client-token is unauthorized."),
    UNAUTHORIZED_CLIENT_TOKEN_CLAIM("9003", "Client-token is unauthorized.");

    private final String code;
    private final String message;

}
