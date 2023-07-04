package com.yolt.sample.app;

import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import nl.ing.lovebird.clienttokens.test.TestClientTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ExampleIntegrationTest {

    @Autowired
    private TestRestTemplate resttemplate;

    @Autowired
    private TestClientTokens testClientTokens;

    final UUID clientGroupId = UUID.randomUUID();
    final UUID clientId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    @Test
    void testGreet() {
        ResponseEntity<String> response = resttemplate.getForEntity("/greet", String.class);
        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Hello world");
    }

    @Test
    void testGreetWithClientToken() {
        ClientToken token = testClientTokens.createClientToken(clientGroupId, clientId);

        ResponseEntity<String> response = resttemplate.exchange(RequestEntity
                .get(URI.create("/greet/client-token"))
                .header("client-token", token.getSerialized())
                .build(), String.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Hello " + clientId);
    }

    @Test
    void testGreetWithClientUserToken() {
        ClientUserToken token = testClientTokens.createClientUserToken(clientGroupId, clientId, userId);

        ResponseEntity<String> response = resttemplate.exchange(RequestEntity
                .get(URI.create("/greet/client-user-token"))
                .header("client-token", token.getSerialized())
                .build(), String.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Hello " + userId + " from " + clientId);
    }

    @Test
    void testGreetWithRestrictedClientUserToken() {
        ClientUserToken token = testClientTokens.createClientUserToken(clientGroupId, clientId, userId,
                claims -> claims.setClaim(ClientTokenConstants.EXTRA_CLAIM_ISSUED_FOR, "secret-service"));

        ResponseEntity<String> response = resttemplate.exchange(RequestEntity
                .get(URI.create("/restricted/client-user-token"))
                .header("client-token", token.getSerialized())
                .build(), String.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Hello secret agent " + userId + " from " + clientId);
    }

}
