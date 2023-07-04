package nl.ing.lovebird.clienttokens.autoconfigure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.requester.service.ClientTokenRequesterService;
import nl.ing.lovebird.clienttokens.requester.service.TokensRestClient;
import nl.ing.lovebird.clienttokens.verification.ClientTokenParser;
import nl.ing.lovebird.secretspipeline.VaultKeys;
import org.jose4j.jwk.RsaJsonWebKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(ClientTokenRequesterService.class)
@ConditionalOnProperty(name = "yolt.client-token.requester.vault-based-secret.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class ClientTokenRequesterVaultAutoConfiguration {

    private final ClientTokenParser clientTokenParser;
    private final TokensRestClient tokensRestClient;
    private final VaultKeys vaultKeys;

    @Bean("vaultClientTokenRequester")
    public ClientTokenRequesterService createService(
            @Value("${tokens.client-token-requester.custom-requesting-service:${spring.application.name}}") final String service) {
        RsaJsonWebKey rsaJsonWebKey = vaultKeys.getRsaJsonWebKey("client-token-req-jwks");
        return new ClientTokenRequesterService(tokensRestClient, rsaJsonWebKey, service, clientTokenParser);
    }

}
