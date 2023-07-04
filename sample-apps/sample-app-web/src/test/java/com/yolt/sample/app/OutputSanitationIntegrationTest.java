package com.yolt.sample.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.GET;

/**
 * To avoid leaking PII the yolt-service-starter is configured to redact PII from logs. This is demonstrated here.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(OutputCaptureExtension.class)
class OutputSanitationIntegrationTest {

    private static final String URL_SUFFIX = "?username={username}&password={password}";
    private static final String SECRET = "secret";

    @LocalServerPort
    int port;
    @Autowired
    private RestTemplateBuilder builder;
    private RestTemplate resttemplate;
    private HttpHeaders headers;
    private Map<String, String> params;
    private HttpEntity<Object> requestEntity;

    @BeforeEach
    void setup() {
        headers = new HttpHeaders();
        headers.add("accesstoken", SECRET);
        requestEntity = new HttpEntity<>(headers);
        params = new HashMap<>();
        params.put("username", SECRET);
        params.put("password", SECRET);

        resttemplate = builder
                .rootUri("http://localhost:" + port)
                .setConnectTimeout(Duration.ofSeconds(1))
                .setReadTimeout(Duration.ofSeconds(1))
                .build();
    }

    @Test
    void testClientError(CapturedOutput output) {
        HttpClientErrorException e = assertThrows(
                HttpClientErrorException.class,
                () -> resttemplate.exchange("/clientError" + URL_SUFFIX, GET, requestEntity, String.class, params));
        assertThat(e.toString()).doesNotContain(SECRET);
        assertThat(e.getResponseBodyAsString()).doesNotContain(SECRET);
        assertThat(e.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
        assertThat(output.getAll()).doesNotContain(SECRET);
    }

    @Test
    void testServerError(CapturedOutput output) {
        HttpServerErrorException e = assertThrows(
                HttpServerErrorException.class,
                () -> resttemplate.exchange("/serverError" + URL_SUFFIX, GET, requestEntity, String.class, params));
        assertThat(e.toString()).doesNotContain(SECRET);
        assertThat(e.getResponseBodyAsString()).doesNotContain(SECRET);
        assertThat(e.getStatusCode()).isEqualByComparingTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(output.getAll()).doesNotContain(SECRET);
    }

    @Test
    void testTimeoutError(CapturedOutput output) {
        ResourceAccessException e = assertThrows(
                ResourceAccessException.class,
                () -> resttemplate.exchange("/timeoutError" + URL_SUFFIX, GET, requestEntity, String.class, params));
        assertThat(e.toString()).doesNotContain(SECRET);
        assertThat(output.getAll()).doesNotContain(SECRET);
    }

    @Test
    void testBodyValidationError(CapturedOutput output) {
        ExceptionalController.SomeBody body = new ExceptionalController.SomeBody(); // Fails @NotNull constraint
        HttpClientErrorException e = assertThrows(
                HttpClientErrorException.class,
                () -> resttemplate.postForEntity("/bodyError" + URL_SUFFIX, new HttpEntity<>(body, headers), String.class, params));
        assertThat(e.toString()).doesNotContain(SECRET);
        assertThat(e.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
        assertThat(output.getAll()).doesNotContain(SECRET);
    }

    @Test
    void testBodyMalformedError() {
        // Can not validate output rule, as spring MVC logs HttpMessageNotReadableException: JSON parse error: Unrecognized token 'secret'
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpClientErrorException e = assertThrows(
                HttpClientErrorException.class,
                () -> resttemplate.postForEntity("/bodyError" + URL_SUFFIX, new HttpEntity<>(SECRET, headers), String.class, params));
        assertThat(e.toString()).doesNotContain(SECRET);
        assertThat(e.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
    }

}
