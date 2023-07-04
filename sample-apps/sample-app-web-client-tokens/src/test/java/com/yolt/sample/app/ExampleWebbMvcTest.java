package com.yolt.sample.app;

import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import nl.ing.lovebird.clienttokens.test.TestClientTokens;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GreetingController.class)
class ExampleWebbMvcTest {

    @Autowired
    private MockMvc mockmvc;

    @Autowired
    private TestClientTokens testClientTokens;

    final UUID clientGroupId = UUID.randomUUID();
    final UUID clientId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    @Test
    void testGreet() throws Exception {
        mockmvc.perform(get("/greet"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello world"));
    }

    @Test
    void testGreetWithClientToken() throws Exception {
        ClientToken token = testClientTokens.createClientToken(clientGroupId, clientId);

        mockmvc.perform(get("/greet/client-token")
                        .header("client-token", token.getSerialized()))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello " + clientId));
    }

    @Test
    void testGreetWithClientUserToken() throws Exception {
        ClientUserToken token = testClientTokens.createClientUserToken(clientGroupId, clientId, userId);

        mockmvc.perform(get("/greet/client-user-token")
                        .header("client-token", token.getSerialized()))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello " + userId + " from " + clientId));

    }

    @Test
    void testGreetWithRestrictedClientUserToken() throws Exception {
        ClientUserToken token = testClientTokens.createClientUserToken(clientGroupId, clientId, userId,
                claims -> claims.setClaim(ClientTokenConstants.EXTRA_CLAIM_ISSUED_FOR, "secret-service"));

        mockmvc.perform(get("/restricted/client-user-token")
                        .header("client-token", token.getSerialized()))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello secret agent " + userId + " from " + clientId));

    }

}
