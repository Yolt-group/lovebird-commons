package nl.ing.lovebird.clienttokens.autoconfigure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.annotations.VerifiedClientToken;
import nl.ing.lovebird.clienttokens.verification.ClientTokenParser;
import nl.ing.lovebird.errorhandling.ExceptionHandlingService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.lang.JoseException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootApplication
@RestController
@RequestMapping("/client-token")
public class ClientTokenTestApp {

    static final RSAPrivateKey SIGNATURE_PRIVATE_KEY;
    static final String SIGNATURE_KID = UUID.randomUUID().toString();
    static final String CLIENT_TOKEN_SERIALIZATION_TEMPLATE = "serialized: %s; claims: %s";
    static final String JSON_WEB_KEY_SET;

    static {
        Security.addProvider(new BouncyCastleProvider());
        try {
            RsaJsonWebKey jsonWebKey = TestTokenCreator.generateRsaJsonWebKey(ClientTokenTestApp.SIGNATURE_KID);
            SIGNATURE_PRIVATE_KEY = jsonWebKey.getRsaPrivateKey();
            JSON_WEB_KEY_SET = new JsonWebKeySet(jsonWebKey).toJson();
        } catch (JoseException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public ExceptionHandlingService exceptionHandlingService() {
        return new ExceptionHandlingService("TEST");
    }

    @Bean
    public ClientTokenParser clientTokenParser() {
        return new ClientTokenParser(JSON_WEB_KEY_SET);
    }

    @GetMapping("/required")
    public String required(@VerifiedClientToken ClientToken clientToken)
            throws JsonProcessingException {
        String serialized = clientToken.getSerialized();
        String claims = new ObjectMapper().writeValueAsString(clientToken.getClaimsMap());
        return String.format(CLIENT_TOKEN_SERIALIZATION_TEMPLATE, serialized, claims);
    }

    @GetMapping("/restricted")
    public String restricted(@VerifiedClientToken(restrictedTo = "TEST") ClientToken clientToken)
            throws JsonProcessingException {
        assertThat(clientToken.getIssuedForClaim()).isEqualTo("TEST");
        String serialized = clientToken.getSerialized();
        String claims = new ObjectMapper().writeValueAsString(clientToken.getClaimsMap());
        return String.format(CLIENT_TOKEN_SERIALIZATION_TEMPLATE, serialized, claims);
    }
}
