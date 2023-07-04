package nl.ing.lovebird.errorhandling.config;

import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.errorhandling.BaseErrorConstants;
import nl.ing.lovebird.errorhandling.ErrorDTO;
import nl.ing.lovebird.errorhandling.ErrorInfoWithDeveloperMessage;
import nl.ing.lovebird.errorhandling.ExceptionHandlingService;
import nl.ing.lovebird.errorhandling.exception.UserNotFoundException;
import org.springframework.core.MethodParameter;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.NotAcceptableStatusException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import java.util.concurrent.ExecutionException;

import static org.slf4j.event.Level.WARN;

@ControllerAdvice
@RequiredArgsConstructor
public final class BaseExceptionHandlers {

    private final ExceptionHandlingService service;

    /**
     * Spring handles some stuff before it goes into the Callable, so these are not wrapped.
     */
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(TaskRejectedException.class)
    @ResponseBody
    public ErrorDTO handleTaskRejectedException(final TaskRejectedException ex) {
        return service.logAndConstruct(BaseErrorConstants.SERVER_BUSY, ex);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ServerWebInputException.class)
    @ResponseBody
    public ErrorDTO handleServerWebInputException(final ServerWebInputException ex) {
        return createMethodParameterErrorDto(ex, ex.getMethodParameter());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public ErrorDTO handleMethodArgumentTypeMismatchException(final MethodArgumentTypeMismatchException ex) {
        return createMethodParameterErrorDto(ex, ex.getParameter());
    }

    private ErrorDTO createMethodParameterErrorDto(Exception ex, MethodParameter parameter) {
        BaseErrorConstants errorInfo = BaseErrorConstants.ARGUMENT_TYPE_MISMATCH;
        boolean isHttpHeader = parameter != null && parameter.hasParameterAnnotation(RequestHeader.class);
        boolean isPathVariable = parameter != null && parameter.hasParameterAnnotation(PathVariable.class);
        if (isHttpHeader) {
            return service.logAndConstruct(WARN, ErrorInfoWithDeveloperMessage.fromErrorConstantWithHttpHeader(errorInfo, parameter.getParameterName()), ex);
        } else if (isPathVariable) {
            return service.logAndConstruct(WARN, ErrorInfoWithDeveloperMessage.fromErrorConstantWithPathVariable(errorInfo, parameter.getParameterName()), ex);
        } else {
            String parameterName = parameter != null ? parameter.getParameterName() : null;
            return service.logAndConstruct(WARN, ErrorInfoWithDeveloperMessage.fromErrorConstantWithField(errorInfo, parameterName), ex);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UnsupportedMediaTypeStatusException.class)
    @ResponseBody
    public ErrorDTO handleServerWebInputException(final UnsupportedMediaTypeStatusException ex) {
        return service.logAndConstruct(WARN, BaseErrorConstants.MEDIA_TYPE_NOT_SUPPORTED, ex);
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(MethodNotAllowedException.class)
    @ResponseBody
    public ErrorDTO handleHttpRequestMethodNotSupportedException(final MethodNotAllowedException ex) {
        return service.logAndConstruct(WARN, BaseErrorConstants.METHOD_NOT_SUPPORTED, ex);
    }

    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(NotAcceptableStatusException.class)
    @ResponseBody
    public ErrorDTO handleHttpRequestMethodNotSupportedException(final NotAcceptableStatusException ex) {
        return service.logAndConstruct(WARN, BaseErrorConstants.NOT_ACCEPTABLE, ex);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageConversionException.class)
    @ResponseBody
    public ErrorDTO handleHttpMessageConversionException(final HttpMessageConversionException ex) {
        return service.logAndConstruct(WARN, BaseErrorConstants.REQUEST_BODY_2_DTO_ERROR, ex);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ErrorDTO handleHttpMessageNotReadableException(final HttpMessageNotReadableException ex) {
        return service.logAndConstruct(WARN, BaseErrorConstants.REQUEST_BODY_2_DTO_ERROR, ex);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ErrorDTO handleMethodArgumentNotValidException(final MethodArgumentNotValidException ex) {
        if (!ex.getBindingResult().getAllErrors().isEmpty()) {
            // Just grab the first error, that's leaps and bounds better than not sending anything, and it keeps it simple.
            ObjectError error = ex.getBindingResult().getAllErrors().get(0);
            if (error instanceof FieldError) {
                FieldError fieldError = (FieldError) error;
                return service.logAndConstruct(WARN, ErrorInfoWithDeveloperMessage.fromErrorConstantWithField(BaseErrorConstants.METHOD_ARGUMENT_NOT_VALID, fieldError.getField()), ex);
            }
        }
        return service.logAndConstruct(WARN, BaseErrorConstants.METHOD_ARGUMENT_NOT_VALID, ex);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ExecutionException.class)
    @ResponseBody
    public ErrorDTO unwrapExecutionException(final ExecutionException ee) {
        return service.logAndConstruct(BaseErrorConstants.GENERIC, ee.getCause());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseBody
    public ErrorDTO handleUserNotFoundException(final UserNotFoundException ue) {
        return service.logAndConstruct(BaseErrorConstants.USER_NOT_FOUND, ue);
    }

}
