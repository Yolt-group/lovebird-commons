package nl.ing.lovebird.clienttokens.autoconfigure;

import lombok.experimental.UtilityClass;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;

import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.util.UUID;

@UtilityClass
class TestTokenCreator {

    private static final String CLIENT_ID = "11112222-3333-4444-5555-666677778888";
    private static final Object CLIENT_GROUP_ID = CLIENT_ID;
    private static final String ORIGIN_IP = "127.0.0.1";
    private static final String SIGNING_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA512;
    private static final String EXTRA_CLAIM_SUBJECT_IP = "sub-ip";
    private static final String EXTRA_CLAIM_ISSUED_FOR = "isf";
    private static final String EXTRA_CLAIM_CLIENT_ID = "client-id";
    private static final String EXTRA_CLAIM_CLIENT_GROUP_ID = "client-group-id";
    private static final String EXTRA_CLAIM_AIS = "ais";
    private static final String EXTRA_CLAIM_PIS = "pis";
    private static final String ISSUED_FOR_EXAMPLE = "providers";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    static RsaJsonWebKey generateRsaJsonWebKey(String signatureKid) throws JoseException {
        RsaJsonWebKey jsonWebKey = RsaJwkGenerator.generateJwk(2048, "BC", new SecureRandom());
        jsonWebKey.setKeyId(signatureKid);
        return jsonWebKey;
    }

    static JwtClaims createJwtClaims() {
        JwtClaims claims = new JwtClaims();
        claims.setSubject("client:" + CLIENT_ID);
        claims.setIssuedAt(NumericDate.now());
        NumericDate expirationDate = NumericDate.now();
        expirationDate.addSeconds(120);
        claims.setExpirationTime(expirationDate);
        claims.setJwtId(UUID.randomUUID().toString());
        claims.setClaim(EXTRA_CLAIM_SUBJECT_IP, ORIGIN_IP);
        claims.setClaim(EXTRA_CLAIM_CLIENT_ID, CLIENT_ID);
        claims.setClaim(EXTRA_CLAIM_CLIENT_GROUP_ID, CLIENT_GROUP_ID);
        claims.setClaim(EXTRA_CLAIM_ISSUED_FOR, ISSUED_FOR_EXAMPLE);
        claims.setClaim(EXTRA_CLAIM_AIS, true);
        claims.setClaim(EXTRA_CLAIM_PIS, true);
        claims.setClaim(EXTRA_CLAIM_ISSUED_FOR, ISSUED_FOR_EXAMPLE);
        return claims;
    }

    static JsonWebSignature createJsonWebSignature(
            JwtClaims claims, PrivateKey signaturePrivateKey, String kid
    ) {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue(SIGNING_ALGORITHM);
        jws.setKey(signaturePrivateKey);
        jws.setKeyIdHeaderValue(kid);
        return jws;
    }
}
