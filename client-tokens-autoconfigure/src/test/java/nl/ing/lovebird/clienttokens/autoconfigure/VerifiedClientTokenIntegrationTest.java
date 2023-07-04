package nl.ing.lovebird.clienttokens.autoconfigure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.ing.lovebird.clienttokens.constants.ClientTokenConstants;
import nl.ing.lovebird.errorhandling.ErrorDTO;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static nl.ing.lovebird.clienttokens.autoconfigure.ClientTokenTestApp.CLIENT_TOKEN_SERIALIZATION_TEMPLATE;
import static nl.ing.lovebird.clienttokens.autoconfigure.ClientTokenTestApp.SIGNATURE_KID;
import static nl.ing.lovebird.clienttokens.autoconfigure.ClientTokenTestApp.SIGNATURE_PRIVATE_KEY;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class VerifiedClientTokenIntegrationTest {

    @Autowired
    private TestRestTemplate resttemplate;

    @Test
    void missingClientTokenHeaderResultsIn401Unauthorized() {
        ResponseEntity<ErrorDTO> response =
                resttemplate.getForEntity("/client-token/required", ErrorDTO.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody())
                .isEqualTo(new ErrorDTO("TEST1002", "Missing header"));
    }

    @Test
    void invalidClientTokenHeaderResultsIn401Unauthorized() {
        ResponseEntity<ErrorDTO> response = resttemplate.exchange(
                RequestEntity.get(URI.create("/client-token/required"))
                        .header(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME, "invalid")
                        .build(),
                ErrorDTO.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody())
                .isEqualTo(new ErrorDTO("TEST9001", "The client-token is invalid."));
    }

    @Test
    void validClientTokenHeaderWithWrongIsfClaimResultsIn403Forbidden()
            throws JoseException {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        claims.setClaim(ClientTokenConstants.EXTRA_CLAIM_ISSUED_FOR, "WRONG");
        String clientToken = TestTokenCreator.createJsonWebSignature(
                claims, SIGNATURE_PRIVATE_KEY, SIGNATURE_KID
        ).getCompactSerialization();
        ResponseEntity<ErrorDTO> response = resttemplate.exchange(
                RequestEntity.get(URI.create("/client-token/restricted"))
                        .header(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME, clientToken)
                        .build(),
                ErrorDTO.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody())
                .isEqualTo(new ErrorDTO("TEST9002", "Token requester for client-token is unauthorized."));
    }

    @Test
    void validClientTokenHeaderResults200Ok() throws JoseException, JsonProcessingException {
        JwtClaims claims = TestTokenCreator.createJwtClaims();
        String serialized = TestTokenCreator.createJsonWebSignature(
                claims, SIGNATURE_PRIVATE_KEY, SIGNATURE_KID
        ).getCompactSerialization();
        ResponseEntity<String> response = resttemplate.exchange(
                RequestEntity.get(URI.create("/client-token/required"))
                        .header(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME, serialized)
                        .build(),
                String.class
        );
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String claimsMap = new ObjectMapper().writeValueAsString(claims.getClaimsMap());
        assertThat(response.getBody()).isEqualTo(
                String.format(CLIENT_TOKEN_SERIALIZATION_TEMPLATE, serialized, claimsMap)
        );
    }

}
