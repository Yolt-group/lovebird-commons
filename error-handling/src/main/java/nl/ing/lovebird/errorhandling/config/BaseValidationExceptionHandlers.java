package nl.ing.lovebird.errorhandling.config;

import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.errorhandling.BaseErrorConstants;
import nl.ing.lovebird.errorhandling.ErrorDTO;
import nl.ing.lovebird.errorhandling.ExceptionHandlingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolationException;

import static org.slf4j.event.Level.WARN;

@ControllerAdvice
@RequiredArgsConstructor
public final class BaseValidationExceptionHandlers {

    private final ExceptionHandlingService service;

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ErrorDTO handleConstraintViolationException(final ConstraintViolationException ex) {
        return service.logAndConstruct(WARN, BaseErrorConstants.METHOD_ARGUMENT_NOT_VALID, ex);
    }
}
