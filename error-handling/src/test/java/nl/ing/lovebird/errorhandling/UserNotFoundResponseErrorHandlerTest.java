package nl.ing.lovebird.errorhandling;

import nl.ing.lovebird.errorhandling.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class UserNotFoundResponseErrorHandlerTest {
    private static final List<HttpStatus.Series> ERROR_STATUSES = Arrays.asList(HttpStatus.Series.CLIENT_ERROR, HttpStatus.Series.SERVER_ERROR);

    private final UserNotFoundResponseErrorHandler errorHandler = new UserNotFoundResponseErrorHandler();

    @Test
    void shouldIndicateErrorStatesCorrectly() throws IOException {
        for (HttpStatus status : HttpStatus.values()) {
            final MockClientHttpResponse mockClientHttpResponse = new MockClientHttpResponse("".getBytes(), status);
            if (ERROR_STATUSES.contains(status.series())) {
                assertThat(errorHandler.hasError(mockClientHttpResponse)).isTrue();
            } else {
                assertThat(errorHandler.hasError(mockClientHttpResponse)).isFalse();
            }
        }
    }

    @Test
    void shouldForwardRegular404ErrorWithEmptyResponseBody() {
        final MockClientHttpResponse mockClientHttpResponse = new MockClientHttpResponse(new byte[0], HttpStatus.NOT_FOUND);

        assertThatExceptionOfType(HttpClientErrorException.NotFound.class)
                .isThrownBy(() -> errorHandler.handleError(mockClientHttpResponse));
    }

    @Test
    void shouldForwardRegular404WithEmptyJsonResponse() {
        final MockClientHttpResponse mockClientHttpResponse = new MockClientHttpResponse("{}".getBytes(), HttpStatus.NOT_FOUND);
        mockClientHttpResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        assertThatExceptionOfType(HttpClientErrorException.NotFound.class)
                .isThrownBy(() -> errorHandler.handleError(mockClientHttpResponse));
    }

    @Test
    void shouldInterceptUserNotFound() {
        final MockClientHttpResponse mockClientHttpResponse = new MockClientHttpResponse("{\"code\":\"TR1011\", \"message\":\"User not found\"}".getBytes(), HttpStatus.NOT_FOUND);
        mockClientHttpResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> errorHandler.handleError(mockClientHttpResponse));
    }

    @Test
    void shouldIgnoreParsingErrorsFor500Error() {
        final MockClientHttpResponse mockClientHttpResponse = new MockClientHttpResponse("{{{".getBytes(), HttpStatus.INTERNAL_SERVER_ERROR);
        mockClientHttpResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        assertThatExceptionOfType(HttpServerErrorException.InternalServerError.class)
                .isThrownBy(() -> errorHandler.handleError(mockClientHttpResponse));
    }

    @Test
    void shouldNotIgnoreParsingErrorsFor404Error() {
        final MockClientHttpResponse mockClientHttpResponse = new MockClientHttpResponse("{{{".getBytes(), HttpStatus.NOT_FOUND);
        mockClientHttpResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        assertThatExceptionOfType(RestClientException.class)
                .isThrownBy(() -> errorHandler.handleError(mockClientHttpResponse));
    }

    @Test
    void shouldTrowExceptionForOtherErrors() {
        for (HttpStatus status : HttpStatus.values()) {
            if (!ERROR_STATUSES.contains(status.series())) {
                continue;
            }
            MockClientHttpResponse mockClientHttpResponse = new MockClientHttpResponse("{\"code\":\"TR1010\", \"message\":\"Other error\"}".getBytes(), status);
            mockClientHttpResponse.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            assertThatExceptionOfType(RestClientResponseException.class)
                    .isThrownBy(() -> errorHandler.handleError(mockClientHttpResponse));
        }
    }
}