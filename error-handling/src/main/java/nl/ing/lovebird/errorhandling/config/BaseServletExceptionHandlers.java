package nl.ing.lovebird.errorhandling.config;

import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.errorhandling.BaseErrorConstants;
import nl.ing.lovebird.errorhandling.ErrorDTO;
import nl.ing.lovebird.errorhandling.ErrorInfoWithDeveloperMessage;
import nl.ing.lovebird.errorhandling.ExceptionHandlingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static nl.ing.lovebird.errorhandling.BaseErrorConstants.MISSING_HEADER;
import static org.slf4j.event.Level.WARN;

@ControllerAdvice
@RequiredArgsConstructor
public final class BaseServletExceptionHandlers {

    private final ExceptionHandlingService service;

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    public ErrorDTO handleMissingServletRequestParameterException(final MissingServletRequestParameterException ex) {
        return service.logAndConstruct(WARN, BaseErrorConstants.MISSING_REQUEST_PARAM, ex);
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public ErrorDTO handleHttpRequestMethodNotSupportedException(final HttpRequestMethodNotSupportedException ex) {
        return service.logAndConstruct(WARN, BaseErrorConstants.METHOD_NOT_SUPPORTED, ex);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ServletRequestBindingException.class)
    @ResponseBody
    public ErrorDTO handleServletRequestBindingException(final ServletRequestBindingException ex) {
        return service.logAndConstruct(WARN, BaseErrorConstants.MISSING_HEADER, ex);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseBody
    public ErrorDTO handleMissingRequestHeaderException(final MissingRequestHeaderException ex) {
        return service.logAndConstruct(WARN, ErrorInfoWithDeveloperMessage.fromErrorConstantWithHttpHeader(MISSING_HEADER, ex.getHeaderName()), ex);
    }

}
