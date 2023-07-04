package com.yolt.sample.app;

import nl.ing.lovebird.errorhandling.ErrorDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ExampleIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testGreet() {
        webTestClient
                .get().uri("/greet")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(greeting -> {
                    assertThat(greeting).isEqualTo("Hello world");
                });
    }

    @Test
    void testExceptionalGreet() {
        webTestClient
                .get().uri("/greet/coffee")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.I_AM_A_TEAPOT)
                .expectBody(ErrorDTO.class).value(errorDTO -> {
                    assertThat(errorDTO.getCode()).isEqualTo("EXAMPLE1008");
                    assertThat(errorDTO.getMessage()).isEqualTo("hello coffee drinker");
                });
    }
}