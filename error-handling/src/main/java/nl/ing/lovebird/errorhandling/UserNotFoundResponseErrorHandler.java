package nl.ing.lovebird.errorhandling;

import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.errorhandling.exception.UserNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.Nullable;
import org.springframework.web.client.ExtractingResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static nl.ing.lovebird.errorhandling.BaseErrorConstants.USER_NOT_FOUND;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.util.StreamUtils.copyToByteArray;
import static org.springframework.web.client.HttpClientErrorException.create;

/**
 * ResponseErrorHandler for {@see RestTemplate}'s which make calls to services, which are
 * dependencies of Users service.
 * When user-not-found occurs in Users service, the dependencies return 404 and the JSON body contains
 * error code {@see BaseErrorConstants.USER_NOT_FOUND.getCode()}, e.g. "AC1011"
 * {@see UserNotFoundException} is thrown for such condition.
 * <p>
 * {@see UserNotFoundException} is handled in {@see BaseExceptionHandlers} and error code
 * "1011" is used again in the error response
 * <p>
 * Example:
 * Calling sequence: Transactions -> Accounts -> Users
 * 1. Users returns 404 (user not found) for given userId
 * 2. Accounts returns status code 404 with JSON body {"code": "AC1011", ...}
 * 3. Transactions recognizes this response as user-not-found
 * 4.Transactions returns status code 404 with JSON body {"code": "TRC1011", ...}
 */

@Slf4j
public class UserNotFoundResponseErrorHandler extends ExtractingResponseErrorHandler {

    public UserNotFoundResponseErrorHandler() {
        setStatusMapping(singletonMap(NOT_FOUND, HttpClientErrorException.NotFound.class));
        setMessageConverters(singletonList(new UserNotFoundMessageConverter()));
    }

    @Override
    public void handleError(ClientHttpResponse response, HttpStatus statusCode) throws IOException {
        // If there is a response body this will either extract the response and throw an exception.
        super.handleError(response, statusCode);
        // Or if the response body was empty because some end points are not well behaved. We throw
        // one explicitly to avoid breaking the contract.
        throw create(NOT_FOUND, "Not Found", response.getHeaders(), new byte[0], getCharset(response));
    }

    /**
     * Extracts the json body from the 404 exception. Check if it is a 404 with the USER_NOT_FOUND
     * status code and creates a UserNotFoundException if this is the case. Otherwise creates a regular
     * NotFound exception.
     */
    private static class UserNotFoundMessageConverter extends AbstractHttpMessageConverter<Object> {

        private final HttpMessageConverter<Object> converter = new MappingJackson2HttpMessageConverter();

        @Override
        public List<MediaType> getSupportedMediaTypes() {
            return converter.getSupportedMediaTypes();
        }

        @Override
        protected boolean supports(Class<?> clazz) {
            return HttpClientErrorException.NotFound.class.equals(clazz);
        }

        @Override
        protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
            byte[] body = copyToByteArray(inputMessage.getBody());
            HttpInputMessage copyOfInputMessage = new CopyOfHttpInputMessage(body, inputMessage);
            ErrorDTO errorDTO = (ErrorDTO) converter.read(ErrorDTO.class, copyOfInputMessage);

            String statusCode = errorDTO.getCode();
            if (statusCode != null && statusCode.endsWith(USER_NOT_FOUND.getCode())) {
                return new UserNotFoundException();
            }
            throw create(NOT_FOUND, "Not Found", inputMessage.getHeaders(), body, getCharset(copyOfInputMessage));
        }

        @Override
        protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws HttpMessageNotWritableException {
            throw new UnsupportedOperationException("UserNotFoundMessageConverter can only read");
        }

        @Nullable
        private Charset getCharset(HttpInputMessage response) {
            HttpHeaders headers = response.getHeaders();
            MediaType contentType = headers.getContentType();
            return (contentType != null ? contentType.getCharset() : null);
        }

        private static class CopyOfHttpInputMessage implements HttpInputMessage {
            private final byte[] bytes;
            private final HttpInputMessage inputMessage;

            CopyOfHttpInputMessage(byte[] bytes, HttpInputMessage inputMessage) {
                this.bytes = bytes;
                this.inputMessage = inputMessage;
            }

            @Override
            public InputStream getBody() {
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public HttpHeaders getHeaders() {
                return inputMessage.getHeaders();
            }
        }
    }
}
