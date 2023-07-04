package nl.ing.lovebird.errorhandling;

import nl.ing.lovebird.errorhandling.config.BaseExceptionHandlers;
import nl.ing.lovebird.errorhandling.config.BaseServletExceptionHandlers;
import nl.ing.lovebird.errorhandling.config.BaseThrowableExceptionHandlers;
import nl.ing.lovebird.errorhandling.config.BaseThrowableWithSpringSecurityExceptionHandlers;
import nl.ing.lovebird.errorhandling.config.BaseTomcatExceptionHandlers;
import nl.ing.lovebird.errorhandling.config.BaseValidationExceptionHandlers;
import nl.ing.lovebird.errorhandling.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.ServletException;
import javax.validation.ConstraintViolationException;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static nl.ing.lovebird.errorhandling.BaseErrorConstants.GENERIC;
import static nl.ing.lovebird.errorhandling.BaseErrorConstants.METHOD_ARGUMENT_NOT_VALID;
import static nl.ing.lovebird.errorhandling.BaseErrorConstants.MISSING_HEADER;
import static nl.ing.lovebird.errorhandling.BaseErrorConstants.MISSING_REQUEST_PARAM;
import static nl.ing.lovebird.errorhandling.BaseErrorConstants.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest(classes = {
        BaseExceptionHandlersTest.Configuration.class,
        BaseExceptionHandlers.class,
        BaseThrowableExceptionHandlers.class,
        BaseThrowableWithSpringSecurityExceptionHandlers.class,
        BaseValidationExceptionHandlers.class,
        BaseTomcatExceptionHandlers.class,
        BaseServletExceptionHandlers.class
})
@ActiveProfiles("test")
class BaseExceptionHandlersTest {
    private static final Exception EX = new Exception("someMessage");

    @Autowired
    private BaseExceptionHandlers baseHandlers;

    @Autowired
    private BaseThrowableExceptionHandlers baseThrowableHandlers;

    @Autowired
    private BaseThrowableWithSpringSecurityExceptionHandlers baseThrowableWithSpringSecurityHandlers;

    @Autowired
    private BaseValidationExceptionHandlers baseValidationHandlers;

    @Autowired
    private BaseServletExceptionHandlers baseServletExceptionHandlers;

    @Autowired
    private BaseTomcatExceptionHandlers baseTomcatHandlers;

    @Autowired
    private ExceptionHandlingService service;

    private WebRequest webRequest;

    private MockHttpServletResponse response;

    @BeforeEach
    void setup() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        response = new MockHttpServletResponse();
        webRequest = new ServletWebRequest(request, response);
    }

    @Test
    void testServletException(CapturedOutput capturedOutput) throws Exception {
        ServletException ex = new ServletRequestBindingException("foo");
        ErrorDTO dto = baseTomcatHandlers.handleServletException(ex, webRequest, response);

        assertThat(dto.getCode()).isEqualTo("TEST" + GENERIC.getCode());
        assertThat(dto.getMessage()).isEqualTo(GENERIC.getMessage());

        assertThat(capturedOutput).containsSubsequence("Server error (TEST1000): foo");
        assertThat(capturedOutput).containsSubsequence(ex.getClass().getSimpleName());
    }

    @Test
    void testExecutionException(CapturedOutput capturedOutput) {
        ExecutionException ex = new ExecutionException(new ServletRequestBindingException("foo"));
        ErrorDTO dto = baseHandlers.unwrapExecutionException(ex);

        assertThat(dto.getCode()).isEqualTo("TEST" + GENERIC.getCode());
        assertThat(dto.getMessage()).isEqualTo(GENERIC.getMessage());

        assertThat(capturedOutput).containsSubsequence("Server error (TEST1000): foo");
        assertThat(capturedOutput).containsSubsequence(ex.getCause().getClass().getSimpleName());
    }

    @Test
    void testServletRequestBindingException(CapturedOutput capturedOutput) {
        ServletRequestBindingException ex = new ServletRequestBindingException("foo");
        ErrorDTO dto = baseServletExceptionHandlers.handleServletRequestBindingException(ex);

        assertThat(dto.getCode()).isEqualTo("TEST" + MISSING_HEADER.getCode());
        assertThat(dto.getMessage()).isEqualTo(MISSING_HEADER.getMessage());

        assertThat(capturedOutput).containsSubsequence("Missing header (TEST1002): foo");
        assertThat(capturedOutput).containsSubsequence(ex.getClass().getSimpleName());
    }

    @Test
    void testMissingServletRequestParameterException(CapturedOutput capturedOutput) {
        MissingServletRequestParameterException ex = new MissingServletRequestParameterException("foo", "bar");
        ErrorDTO dto = baseServletExceptionHandlers.handleMissingServletRequestParameterException(ex);

        assertThat(dto.getCode()).isEqualTo("TEST" + MISSING_REQUEST_PARAM.getCode());
        assertThat(dto.getMessage()).isEqualTo(MISSING_REQUEST_PARAM.getMessage());

        assertThat(capturedOutput).containsSubsequence("Missing request parameter (TEST1006): Required request parameter 'foo' for method parameter type bar is not present");
        assertThat(capturedOutput).containsSubsequence(ex.getClass().getSimpleName());
    }

    @Test
    void testHandlerConstraintViolationException(CapturedOutput capturedOutput) {
        ConstraintViolationException ex = new ConstraintViolationException("Message", Collections.emptySet());
        ErrorDTO dto = baseValidationHandlers.handleConstraintViolationException(ex);

        assertThat(dto.getCode()).isEqualTo("TEST" + METHOD_ARGUMENT_NOT_VALID.getCode());
        assertThat(dto.getMessage()).isEqualTo(METHOD_ARGUMENT_NOT_VALID.getMessage());

        assertThat(capturedOutput).containsSubsequence("Method argument not valid (request body validation error) (TEST1008): Message");
        assertThat(capturedOutput).containsSubsequence(ex.getClass().getSimpleName());
    }

    @Test
    void testHandlerUserNotFoundException(CapturedOutput capturedOutput) {
        final UUID userId = UUID.randomUUID();
        UserNotFoundException ex = new UserNotFoundException(userId);
        ErrorDTO dto = baseHandlers.handleUserNotFoundException(ex);

        assertThat(dto.getCode()).isEqualTo("TEST" + USER_NOT_FOUND.getCode());
        assertThat(dto.getMessage()).isEqualTo(USER_NOT_FOUND.getMessage());

        assertThat(capturedOutput).containsSubsequence("User not found (TEST1011): User data not found for userId " + userId);
        assertThat(capturedOutput).containsSubsequence(ex.getClass().getSimpleName());
    }

    @Test
    void testHandlerGeneric(CapturedOutput capturedOutput) {
        ErrorDTO dto = baseThrowableHandlers.handleGeneric(EX);

        assertThat(dto.getCode()).isEqualTo("TEST" + GENERIC.getCode());
        assertThat(dto.getMessage()).isEqualTo(GENERIC.getMessage());

        assertThat(capturedOutput).containsSubsequence("Server error (TEST1000): someMessage");
        assertThat(capturedOutput).containsSubsequence(EX.getClass().getSimpleName());
    }

    @Test
    void testHandlerGenericWithSpringSecurity() {
        AccessDeniedException exception = new AccessDeniedException("");
        assertThatThrownBy(() -> baseThrowableWithSpringSecurityHandlers.handleGeneric(exception))
                .isEqualTo(exception);
    }

    @Test
    void testLogAndConstruct(CapturedOutput capturedOutput) {
        Exception ex = new MissingServletRequestParameterException("userId", "UUID");
        ErrorDTO dto = service.logAndConstruct(MISSING_REQUEST_PARAM, ex);

        assertThat(dto.getCode()).isEqualTo("TEST" + MISSING_REQUEST_PARAM.getCode());
        assertThat(dto.getMessage()).isEqualTo(MISSING_REQUEST_PARAM.getMessage());

        assertThat(capturedOutput).containsSubsequence("Missing request parameter (TEST1006): Required request parameter 'userId' for method parameter type UUID is not present");
        assertThat(capturedOutput).containsSubsequence(ex.getClass().getSimpleName());
    }

    @Test
    void testLogAndConstructTraceLogLevel(CapturedOutput capturedOutput) {
        final DummyExceptions.TraceDummyException traceDummyException = new DummyExceptions.TraceDummyException();
        service.logAndConstruct(Level.TRACE, new ErrorInfoImpl(), traceDummyException);

        assertThat(capturedOutput).containsSubsequence("message (TESTcode): dummy");
        assertThat(capturedOutput).containsSubsequence(traceDummyException.getClass().getSimpleName());
    }

    @Test
    void testLogAndConstructDebugLogLevel(CapturedOutput capturedOutput) {
        final DummyExceptions.DebugDummyException debugDummyException = new DummyExceptions.DebugDummyException();
        service.logAndConstruct(Level.DEBUG, new ErrorInfoImpl(), debugDummyException);

        assertThat(capturedOutput).containsSubsequence("message (TESTcode): dummy");
        assertThat(capturedOutput).containsSubsequence(debugDummyException.getClass().getSimpleName());
    }

    @Test
    void testLogAndConstructInfoLogLevel(CapturedOutput capturedOutput) {
        final DummyExceptions.InfoDummyException infoDummyException = new DummyExceptions.InfoDummyException();
        service.logAndConstruct(Level.INFO, new ErrorInfoImpl(), infoDummyException);

        assertThat(capturedOutput).containsSubsequence("message (TESTcode): dummy");
        assertThat(capturedOutput).containsSubsequence(infoDummyException.getClass().getSimpleName());
    }

    @Test
    void testLogAndConstructWarnLogLevel(CapturedOutput capturedOutput) {
        final DummyExceptions.WarnDummyException warnDummyException = new DummyExceptions.WarnDummyException();
        service.logAndConstruct(Level.WARN, new ErrorInfoImpl(), warnDummyException);

        assertThat(capturedOutput).containsSubsequence("message (TESTcode): dummy");
        assertThat(capturedOutput).containsSubsequence(warnDummyException.getClass().getSimpleName());
    }

    @Test
    void testLogAndConstructErrorLogLevel(CapturedOutput capturedOutput) {
        final DummyExceptions.ErrorDummyException errorDummyException = new DummyExceptions.ErrorDummyException();
        service.logAndConstruct(Level.ERROR, new ErrorInfoImpl(), errorDummyException);

        assertThat(capturedOutput).containsSubsequence("message (TESTcode): dummy");
        assertThat(capturedOutput).containsSubsequence(errorDummyException.getClass().getSimpleName());
    }

    @TestConfiguration
    public static class Configuration {

        @Bean
        public ExceptionHandlingService exceptionHandlingService() {
            return new ExceptionHandlingService("TEST");
        }
    }

    private static class ErrorInfoImpl implements ErrorInfo {

        @Override
        public String getCode() {
            return "code";
        }

        @Override
        public String getMessage() {
            return "message";
        }
    }
}

