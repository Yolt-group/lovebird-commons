package nl.ing.lovebird.clienttokens.web;

import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.clienttokens.constants.ErrorConstants;
import nl.ing.lovebird.clienttokens.verification.exception.InvalidClientTokenException;
import nl.ing.lovebird.clienttokens.verification.exception.MissingHeaderException;
import nl.ing.lovebird.clienttokens.verification.exception.UnauthorizedClientTokenClaimException;
import nl.ing.lovebird.clienttokens.verification.exception.UnauthorizedClientTokenRequesterException;
import nl.ing.lovebird.errorhandling.BaseErrorConstants;
import nl.ing.lovebird.errorhandling.ErrorDTO;
import nl.ing.lovebird.errorhandling.ExceptionHandlingService;
import org.slf4j.event.Level;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE - 1) // lowest is actually Integer.MAX_VALUE
public class ClientTokenVerificationExceptionHandlers {

    private final ExceptionHandlingService service;

    @ExceptionHandler(InvalidClientTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorDTO handle(InvalidClientTokenException ex) {
        return service.logAndConstruct(Level.INFO, ErrorConstants.INVALID_CLIENT_TOKEN, ex);
    }

    @ExceptionHandler(MissingHeaderException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public ErrorDTO handle(MissingHeaderException ex) {
        return service.logAndConstruct(Level.INFO, BaseErrorConstants.MISSING_HEADER, ex);
    }

    @ExceptionHandler(UnauthorizedClientTokenRequesterException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorDTO handle(UnauthorizedClientTokenRequesterException ex) {
        return service.logAndConstruct(Level.INFO, ErrorConstants.UNAUTHORIZED_CLIENT_TOKEN_REQUESTER, ex);
    }

    @ExceptionHandler(UnauthorizedClientTokenClaimException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorDTO handle(UnauthorizedClientTokenClaimException ex) {
        return service.logAndConstruct(Level.INFO, ErrorConstants.UNAUTHORIZED_CLIENT_TOKEN_CLAIM, ex);
    }

}
