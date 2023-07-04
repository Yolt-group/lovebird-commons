package nl.ing.lovebird.errorhandling;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BaseErrorConstants implements ErrorInfo {
    GENERIC("1000", "Server error"),
    METHOD_NOT_SUPPORTED("1001", "Method not supported"),
    MISSING_HEADER("1002", "Missing header"),
    INVALID_UUID("1003", "Invalid UUID"),
    SERVER_BUSY("1004", "Server busy"),
    ARGUMENT_TYPE_MISMATCH("1005", "Argument type mismatch"),
    MISSING_REQUEST_PARAM("1006", "Missing request parameter"),
    METHOD_ARGUMENT_NOT_VALID("1008", "Method argument not valid (request body validation error)"),
    REQUEST_BODY_2_DTO_ERROR("1009", "Error converting request body to DTO"),
    USER_NOT_FOUND("1011", "User not found"),
    MEDIA_TYPE_NOT_SUPPORTED("1012", "Media type not supported"),
    NOT_ACCEPTABLE("1013", "Could not find acceptable representation")
    ;

    private final String code;
    private final String message;
}
