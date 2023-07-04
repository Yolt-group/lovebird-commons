package nl.ing.lovebird.clienttokens.autoconfigure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.requester.service.ClientTokenRequesterService;
import nl.ing.lovebird.clienttokens.requester.service.TokensRestClient;
import nl.ing.lovebird.clienttokens.verification.ClientTokenParser;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.lang.JoseException;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static nl.ing.lovebird.clienttokens.autoconfigure.YoltClientTokenProperties.Requester.API_GATEWAY;
import static nl.ing.lovebird.clienttokens.autoconfigure.YoltClientTokenProperties.Requester.CLIENT_GATEWAY;
import static nl.ing.lovebird.clienttokens.autoconfigure.YoltClientTokenProperties.Requester.CONSENT_STARTER;
import static nl.ing.lovebird.clienttokens.autoconfigure.YoltClientTokenProperties.Requester.DEV_PORTAL;
import static nl.ing.lovebird.clienttokens.autoconfigure.YoltClientTokenProperties.Requester.SIGNATURE_PRIVATE_KEY_PREFIX;
import static nl.ing.lovebird.clienttokens.autoconfigure.YoltClientTokenProperties.Requester.SITE_MANAGEMENT;
import static nl.ing.lovebird.clienttokens.autoconfigure.YoltClientTokenProperties.Requester.YOLT_ASSISTANCE_PORTAL;

@AutoConfiguration
@EnableConfigurationProperties(YoltClientTokenProperties.class)
@ConditionalOnClass(ClientTokenRequesterService.class)
@ConditionalOnProperty(name = "yolt.client-token.requester.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class ClientTokenRequesterServiceAutoConfiguration {

    private final TokensRestClient tokensRestClient;
    private final ClientTokenParser clientTokenParser;
    private final YoltClientTokenProperties clientTokenProperties;

    @Bean(name = CLIENT_GATEWAY)
    @ConditionalOnProperty(SIGNATURE_PRIVATE_KEY_PREFIX + CLIENT_GATEWAY)
    public ClientTokenRequesterService createClientGatewayService() {
        return createService(clientTokenProperties.getRequester().getSigningKeys().getClientGateway(), CLIENT_GATEWAY);
    }

    @Bean(name = DEV_PORTAL)
    @ConditionalOnProperty(SIGNATURE_PRIVATE_KEY_PREFIX + DEV_PORTAL)
    public ClientTokenRequesterService createDevPortalService() {
        return createService(clientTokenProperties.getRequester().getSigningKeys().getDevPortal(), DEV_PORTAL);
    }

    @Bean(name = API_GATEWAY)
    @ConditionalOnProperty(SIGNATURE_PRIVATE_KEY_PREFIX + API_GATEWAY)
    public ClientTokenRequesterService createApiGatewayService() {
        return createService(clientTokenProperties.getRequester().getSigningKeys().getApiGateway(), API_GATEWAY);
    }

    @Bean(name = SITE_MANAGEMENT)
    @ConditionalOnProperty(SIGNATURE_PRIVATE_KEY_PREFIX + SITE_MANAGEMENT)
    public ClientTokenRequesterService createSiteManagementService() {
        return createService(clientTokenProperties.getRequester().getSigningKeys().getSiteManagement(), SITE_MANAGEMENT);
    }

    @Bean(name = YOLT_ASSISTANCE_PORTAL)
    @ConditionalOnProperty(SIGNATURE_PRIVATE_KEY_PREFIX + YOLT_ASSISTANCE_PORTAL)
    public ClientTokenRequesterService createYoltAssistancePortalService() {
        return createService(clientTokenProperties.getRequester().getSigningKeys().getYoltAssistancePortal(), YOLT_ASSISTANCE_PORTAL);
    }

    @Bean(name = CONSENT_STARTER)
    @ConditionalOnProperty(SIGNATURE_PRIVATE_KEY_PREFIX + CONSENT_STARTER)
    public ClientTokenRequesterService createConsentStarterService() {
        return createService(clientTokenProperties.getRequester().getSigningKeys().getConsentStarter(), CONSENT_STARTER);
    }

    private ClientTokenRequesterService createService(String signingJWK, String service) {
        try {
            RsaJsonWebKey jsonWebKey = (RsaJsonWebKey) PublicJsonWebKey.Factory.newPublicJwk(signingJWK);
            ClientTokenRequesterService clientTokenRequesterService = new ClientTokenRequesterService(tokensRestClient, jsonWebKey, service, clientTokenParser);
            log.info("Registered a clientTokenService bean for service: {}", service);
            return clientTokenRequesterService;
        } catch (JoseException e) {
            String message = String.format("Cannot parse the signingKeyJwk into a private-key JWK for requester: %s, exceptionClass: %s",
                    service, e.getClass().getName());
            throw new IllegalStateException(message);
        }
    }

}
