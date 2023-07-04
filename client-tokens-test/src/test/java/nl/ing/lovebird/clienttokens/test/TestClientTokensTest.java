package nl.ing.lovebird.clienttokens.test;

import nl.ing.lovebird.clienttokens.ClientGroupToken;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.function.Consumer;

import static nl.ing.lovebird.clienttokens.constants.ClientTokenConstants.EXTRA_CLAIM_ISSUED_FOR;
import static org.assertj.core.api.Assertions.assertThat;

class TestClientTokensTest {

    private static RsaJsonWebKey signatureJwk;
    private TestClientTokens testClientTokens;

    @BeforeAll
    static void setupSignatureJwk() throws JoseException {
        // Slow. Create only once.
        signatureJwk = RsaJwkGenerator.generateJwk(2048);
    }

    @BeforeEach
    void setup() {
        testClientTokens = new TestClientTokens("junit", signatureJwk);
    }

    final UUID clientGroupId = UUID.randomUUID();
    final UUID clientId = UUID.randomUUID();
    final UUID userId = UUID.randomUUID();

    @Test
    void shouldCreateClientGroupToken() {
        ClientGroupToken token = testClientTokens.createClientGroupToken(clientGroupId);
        assertThat(token.getClientGroupIdClaim()).isEqualTo(clientGroupId);
    }

    @Test
    void shouldMutateClientGroupToken() {
        Consumer<JwtClaims> mutator = jwtClaims -> jwtClaims.setClaim(EXTRA_CLAIM_ISSUED_FOR, "test-service");
        ClientGroupToken token = testClientTokens.createClientGroupToken(clientGroupId, mutator);
        assertThat(token.getIssuedForClaim()).isEqualTo("test-service");
    }

    @Test
    void shouldCreateClientToken() {
        ClientToken token = testClientTokens.createClientToken(clientGroupId, clientId);
        assertThat(token.getClientGroupIdClaim()).isEqualTo(clientGroupId);
        assertThat(token.getClientIdClaim()).isEqualTo(clientId);
    }

    @Test
    void shouldMutateClientToken() {
        Consumer<JwtClaims> mutator = jwtClaims -> jwtClaims.setClaim(EXTRA_CLAIM_ISSUED_FOR, "test-service");
        ClientToken token = testClientTokens.createClientToken(clientGroupId, clientId, mutator);
        assertThat(token.getIssuedForClaim()).isEqualTo("test-service");
    }

    @Test
    void shouldCreateClientUserToken() {
        ClientUserToken token = testClientTokens.createClientUserToken(clientGroupId, clientId, userId);
        assertThat(token.getClientGroupIdClaim()).isEqualTo(clientGroupId);
        assertThat(token.getClientIdClaim()).isEqualTo(clientId);
        assertThat(token.getUserIdClaim()).isEqualTo(userId);
    }

    @Test
    void shouldMutateClientUserToken() {
        Consumer<JwtClaims> mutator = jwtClaims -> jwtClaims.setClaim(EXTRA_CLAIM_ISSUED_FOR, "test-service");
        ClientToken token = testClientTokens.createClientUserToken(clientGroupId, clientId, userId, mutator);
        assertThat(token.getIssuedForClaim()).isEqualTo("test-service");
    }
}