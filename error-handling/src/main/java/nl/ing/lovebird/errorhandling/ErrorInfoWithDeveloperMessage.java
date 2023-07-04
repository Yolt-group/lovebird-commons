package nl.ing.lovebird.errorhandling;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * This class can be used when 4xx errors occur to provide a client developer with more information about what
 * exactly went wrong.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorInfoWithDeveloperMessage implements ErrorInfo {

    @NonNull
    private final ErrorInfo errorInfo;
    private final String fieldName;
    private final String httpHeaderName;
    private final String pathVariableName;

    @Override
    public String getCode() {
        return errorInfo.getCode();
    }

    @Override
    public String getMessage() {
        String message = errorInfo.getMessage();
        if (fieldName != null) {
            message += ". Offending field: " + fieldName;
        }
        if (httpHeaderName != null) {
            message += ". Offending http header: " + httpHeaderName;
        }
        if (pathVariableName != null) {
            message += ". Offending path parameter: " + pathVariableName;
        }
        return message;
    }

    public static ErrorInfoWithDeveloperMessage fromErrorConstantWithHttpHeader(ErrorInfo errorInfo, String httpHeaderName) {
        return new ErrorInfoWithDeveloperMessage(errorInfo, null, httpHeaderName, null);
    }

    public static ErrorInfoWithDeveloperMessage fromErrorConstantWithPathVariable(ErrorInfo errorInfo, String pathVariableName) {
        return new ErrorInfoWithDeveloperMessage(errorInfo, null, null, pathVariableName);
    }

    public static ErrorInfoWithDeveloperMessage fromErrorConstantWithField(ErrorInfo errorInfo, String fieldName) {
        return new ErrorInfoWithDeveloperMessage(errorInfo, fieldName, null, null);
    }

}
