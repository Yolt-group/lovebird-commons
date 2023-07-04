package nl.ing.lovebird.clienttokens.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import org.jose4j.jwt.JwtClaims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static nl.ing.lovebird.clienttokens.autoconfigure.ClientTokenTestApp.CLIENT_TOKEN_SERIALIZATION_TEMPLATE;
import static nl.ing.lovebird.clienttokens.autoconfigure.ClientTokenTestApp.SIGNATURE_KID;
import static nl.ing.lovebird.clienttokens.autoconfigure.ClientTokenTestApp.SIGNATURE_PRIVATE_KEY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientTokenTestApp.class)
class VerifiedClientTokenWebMvcTest {

    @Autowired
    private MockMvc mockmvc;

    @Test
    void missingClientTokenHeaderResultsIn401Unauthorized() throws Exception {
        mockmvc.perform(get("/client-token/required"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TEST1002"))
                .andExpect(jsonPath("$.message").value("Missing header"));
    }

    @Test
    void invalidClientTokenHeaderResultsIn401Unauthorized() throws Exception {
        mockmvc.perform(get("/client-token/required")
                        .header(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME, "invalid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("TEST9001"))
                .andExpect(jsonPath("$.message").value("The client-token is invalid."));
    }

    @Test
    void validClientTokenHeaderWithWrongIsfClaimResultsIn403Forbidden() throws Exception {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        claims.setClaim(ClientTokenConstants.EXTRA_CLAIM_ISSUED_FOR, "WRONG");
        String clientToken = TestTokenCreator.createJsonWebSignature(
                claims, SIGNATURE_PRIVATE_KEY, SIGNATURE_KID
        ).getCompactSerialization();

        mockmvc.perform(get("/client-token/restricted")
                        .header(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME, clientToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("TEST9002"))
                .andExpect(jsonPath("$.message").value("Token requester for client-token is unauthorized."));
    }

    @Test
    void validClientTokenHeaderResults200Ok() throws Exception {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        String serialized = TestTokenCreator.createJsonWebSignature(
                claims, SIGNATURE_PRIVATE_KEY, SIGNATURE_KID
        ).getCompactSerialization();
        String claimsMap = new ObjectMapper().writeValueAsString(claims.getClaimsMap());

        mockmvc.perform(get("/client-token/required")
                        .header(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME, serialized))
                .andExpect(status().isOk())
                .andExpect(content().string(String.format(CLIENT_TOKEN_SERIALIZATION_TEMPLATE, serialized, claimsMap)));
    }

}
