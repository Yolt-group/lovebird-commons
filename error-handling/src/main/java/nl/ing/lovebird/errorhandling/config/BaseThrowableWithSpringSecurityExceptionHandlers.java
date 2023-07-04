package nl.ing.lovebird.errorhandling.config;

import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.errorhandling.BaseErrorConstants;
import nl.ing.lovebird.errorhandling.ErrorDTO;
import nl.ing.lovebird.errorhandling.ExceptionHandlingService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public final class BaseThrowableWithSpringSecurityExceptionHandlers {

    private final ExceptionHandlingService service;

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ErrorDTO handleGeneric(final Throwable ex) throws Throwable {
        if (ex instanceof AccessDeniedException) {
            // Do not interfere with Spring Security exception handling flows by rethrowing exception
            throw ex;
        }
        return service.logAndConstruct(BaseErrorConstants.GENERIC, ex);
    }

}
