package com.yolt.sample.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ExampleIntegrationTest {

    @Autowired
    private TestRestTemplate resttemplate;

    @Test
    void testGreet() {
        ResponseEntity<String> response = resttemplate.getForEntity("/greet", String.class);
        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Hello world");
    }

    @Test
    void testExceptionalGreet() {
        ResponseEntity<String> response = resttemplate.getForEntity("/greet/coffee", String.class);
        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.I_AM_A_TEAPOT);
        assertThat(response.getBody()).isEqualTo("{\"code\":\"EXAMPLE1008\",\"message\":\"hello coffee drinker\"}");
    }
}
