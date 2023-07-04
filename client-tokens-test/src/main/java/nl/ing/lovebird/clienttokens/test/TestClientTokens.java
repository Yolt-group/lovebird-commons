package nl.ing.lovebird.clienttokens.test;

import lombok.SneakyThrows;
import nl.ing.lovebird.clienttokens.ClientGroupToken;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Creates signed client (user) tokens for testing purposes.
 */
public class TestClientTokens {

    private final Consumer<JwtClaims> NOOP = jwtClaims -> {
    };

    private final String issueForService;
    private final RsaJsonWebKey signatureJwk;

    public TestClientTokens(String issueForService, RsaJsonWebKey signatureJwk) {
        this.issueForService = issueForService;
        this.signatureJwk = signatureJwk;
    }

    @SneakyThrows
    public ClientGroupToken createClientGroupToken(UUID clientGroupId) {
        return createClientGroupToken(clientGroupId, NOOP);
    }

    @SneakyThrows
    public ClientGroupToken createClientGroupToken(UUID clientGroupId, Consumer<JwtClaims> claimsMutator) {
        JwtClaims claims = TestJwtClaims.createClientGroupClaims(issueForService, clientGroupId);
        claimsMutator.accept(claims);
        JsonWebSignature jws = signClaims(claims);
        return new ClientGroupToken(jws.getCompactSerialization(), claims);
    }

    @SneakyThrows
    public ClientToken createClientToken(UUID clientGroupId, UUID clientId) {
        return createClientToken(clientGroupId, clientId, NOOP);
    }

    @SneakyThrows
    public ClientToken createClientToken(UUID clientGroupId, UUID clientId, Consumer<JwtClaims> claimsMutator) {
        JwtClaims claims = TestJwtClaims.createClientClaims(issueForService, clientGroupId, clientId);
        claimsMutator.accept(claims);
        JsonWebSignature jws = signClaims(claims);
        return new ClientToken(jws.getCompactSerialization(), claims);
    }

    @SneakyThrows
    public ClientUserToken createClientUserToken(UUID clientGroupId, UUID clientId, UUID userId) {
        return createClientUserToken(clientGroupId, clientId, userId, NOOP);
    }

    @SneakyThrows
    public ClientUserToken createClientUserToken(UUID clientGroupId, UUID clientId, UUID userId, Consumer<JwtClaims> claimsMutator) {
        JwtClaims claims = TestJwtClaims.createClientUserClaims(issueForService, clientGroupId, clientId, userId);
        claimsMutator.accept(claims);
        JsonWebSignature jws = signClaims(claims);
        return new ClientUserToken(jws.getCompactSerialization(), claims);
    }

    private JsonWebSignature signClaims(JwtClaims claims) {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_PSS_USING_SHA512);
        jws.setKey(signatureJwk.getPrivateKey());
        jws.setKeyIdHeaderValue(signatureJwk.getKeyId());
        return jws;
    }

}
