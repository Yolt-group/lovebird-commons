package nl.ing.lovebird.clienttokens.autoconfigure.test;

import lombok.SneakyThrows;
import nl.ing.lovebird.clienttokens.autoconfigure.ClientTokenParserAutoConfiguration;
import nl.ing.lovebird.clienttokens.test.TestClientTokens;
import nl.ing.lovebird.clienttokens.verification.ClientTokenParser;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(before = ClientTokenParserAutoConfiguration.class)
@ConditionalOnClass({ClientTokenParser.class, TestClientTokens.class})
public class ClientTokenTestParserAutoConfiguration {

    private static RsaJsonWebKey jsonWebKeySingleton;

    @SneakyThrows
    private synchronized RsaJsonWebKey getJsonWebKey() {
        if (jsonWebKeySingleton != null) {
            return jsonWebKeySingleton;
        }
        jsonWebKeySingleton = RsaJwkGenerator.generateJwk(2048);
        return jsonWebKeySingleton;
    }

    @Bean
    public TestClientTokens testClientTokens(@Value("${spring.application.name}") final String service) {
        return new TestClientTokens(service, getJsonWebKey());
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientTokenParser testClientTokenParser() {
        RsaJsonWebKey jsonWebKey = getJsonWebKey();
        JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(jsonWebKey);
        return new ClientTokenParser(jsonWebKeySet.toJson());
    }

}
