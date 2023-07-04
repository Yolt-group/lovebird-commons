package nl.ing.lovebird.clienttokens.autoconfigure;

import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.clienttokens.requester.service.TokensRestClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(name = "service.tokens.signature-jwks")
@ConditionalOnClass(TokensRestClient.class)
@EnableConfigurationProperties(ServiceTokenProperties.class)
@RequiredArgsConstructor
public class ClientTokenRestClientAutoConfiguration {

    private final ServiceTokenProperties properties;

    @Bean
    public TokensRestClient tokensRestClient(RestTemplateBuilder builder) {
        return new TokensRestClient(builder.rootUri(properties.getUrl()).build());
    }
}
