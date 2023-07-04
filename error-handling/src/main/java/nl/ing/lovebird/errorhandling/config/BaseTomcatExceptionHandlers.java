package nl.ing.lovebird.errorhandling.config;

import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.errorhandling.BaseErrorConstants;
import nl.ing.lovebird.errorhandling.ErrorDTO;
import nl.ing.lovebird.errorhandling.ExceptionHandlingService;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import static nl.ing.lovebird.errorhandling.BaseErrorConstants.GENERIC;
import static org.slf4j.event.Level.WARN;

@ControllerAdvice
@RequiredArgsConstructor
public final class BaseTomcatExceptionHandlers {

    private final ExceptionHandlingService service;

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ClientAbortException.class)
    @ResponseBody
    public void handleClientAbortException(final ClientAbortException ex) {
        service.logAndConstruct(WARN, GENERIC, ex);
    }

    @ExceptionHandler(ServletException.class)
    @ResponseBody
    public ErrorDTO handleServletException(final ServletException ex, final WebRequest req, final HttpServletResponse response) throws Exception {
        response.setStatus(getStatusCodeOfServletException(ex, req));
        return service.logAndConstruct(BaseErrorConstants.GENERIC, ex);
    }

    private int getStatusCodeOfServletException(Exception ex, WebRequest req) throws Exception {
        if (ex instanceof ServletException) {
            ResponseEntityExceptionHandler responseEntityExceptionHandler = new ResponseEntityExceptionHandler() {
            };
            ResponseEntity<Object> responseEntity = responseEntityExceptionHandler.handleException(ex, req);
            return responseEntity.getStatusCode().value();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
