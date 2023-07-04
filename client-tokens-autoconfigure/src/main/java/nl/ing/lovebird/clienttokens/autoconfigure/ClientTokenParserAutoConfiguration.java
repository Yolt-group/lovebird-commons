package nl.ing.lovebird.clienttokens.autoconfigure;

import nl.ing.lovebird.clienttokens.verification.ClientTokenParser;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ClientTokenParser.class)
@EnableConfigurationProperties(ServiceTokenProperties.class)
public class ClientTokenParserAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "service.tokens.signature-jwks")
    public ClientTokenParser createClientTokenParser(ServiceTokenProperties properties) {
        return new ClientTokenParser(properties.getSignatureJwks());
    }
}
