package com.yolt.sample.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * To avoid leaking PII the yolt-service-starter is configured to redact PII from logs. This is demonstrated here.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(OutputCaptureExtension.class)
class OutputSanitationIntegrationTest {

    private static final String URL_SUFFIX = "?username={username}&password={password}";
    private static final String SECRET = "secret";

    @Autowired
    private WebTestClient webTestClient;

    private Map<String, String> params;

    @BeforeEach
    void setup() {
        params = new HashMap<>();
        params.put("username", SECRET);
        params.put("password", SECRET);
    }

    @Test
    void testClientError(CapturedOutput output) {
        webTestClient.get().uri("/clientError" + URL_SUFFIX, params)
                .accept(MediaType.APPLICATION_JSON)
                .header("accesstoken", SECRET)
                .exchange()
                .expectStatus().isBadRequest()
                        .expectBody(String.class).value(s -> assertThat(s).doesNotContain(SECRET));
        assertThat(output.getAll()).doesNotContain(SECRET);
    }

    @Test
    void testServerError(CapturedOutput output) {
        webTestClient.get()
                .uri("/serverError" + URL_SUFFIX, params)
                .header("accesstoken", SECRET)
                .exchange()
                .expectStatus().isEqualTo(INTERNAL_SERVER_ERROR)
                .expectBody(String.class).value(s -> assertThat(s).doesNotContain(SECRET));
        assertThat(output.getAll()).doesNotContain(SECRET);
    }

    @Test
    void testTimeoutError(CapturedOutput output) {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> webTestClient.get()
                .uri("/timeoutError" + URL_SUFFIX, params)
                .header("accesstoken", SECRET)
                .exchange());

        assertThat(e.getMessage()).doesNotContain(SECRET);
        assertThat(output.getAll()).doesNotContain(SECRET);
    }

    @Test
    void testBodyValidationError(CapturedOutput output) {

        webTestClient.post().uri("/bodyError" + URL_SUFFIX, params)
                .bodyValue(new ExceptionalController.SomeBody()) // Fails @NotNull constraint
                .accept(MediaType.APPLICATION_JSON)
                .header("accesstoken", SECRET)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).value(s -> assertThat(s).doesNotContain(SECRET));

        assertThat(output.getAll()).doesNotContain(SECRET);
    }

    @Test
    void testBodyMalformedError() {
        // Can not validate output rule, as spring MVC logs HttpMessageNotReadableException: JSON parse error: Unrecognized token 'secret'
        webTestClient.post().uri("/bodyError" + URL_SUFFIX, params)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(SECRET)
                .accept(MediaType.APPLICATION_JSON)
                .header("accesstoken", SECRET)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).value(s -> assertThat(s).doesNotContain(SECRET));
    }

}
