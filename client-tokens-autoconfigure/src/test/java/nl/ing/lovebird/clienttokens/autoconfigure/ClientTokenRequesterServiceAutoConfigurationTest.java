package nl.ing.lovebird.clienttokens.autoconfigure;

import lombok.SneakyThrows;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.file.Paths;
import java.util.UUID;

import static java.nio.file.Files.readAllBytes;
import static nl.ing.lovebird.clienttokens.autoconfigure.YoltClientTokenProperties.Requester.API_GATEWAY;
import static nl.ing.lovebird.clienttokens.autoconfigure.YoltClientTokenProperties.Requester.CLIENT_GATEWAY;
import static nl.ing.lovebird.clienttokens.autoconfigure.YoltClientTokenProperties.Requester.CONSENT_STARTER;
import static nl.ing.lovebird.clienttokens.autoconfigure.YoltClientTokenProperties.Requester.DEV_PORTAL;
import static nl.ing.lovebird.clienttokens.autoconfigure.YoltClientTokenProperties.Requester.SITE_MANAGEMENT;
import static nl.ing.lovebird.clienttokens.autoconfigure.YoltClientTokenProperties.Requester.YOLT_ASSISTANCE_PORTAL;
import static org.assertj.core.api.Assertions.assertThat;

class ClientTokenRequesterServiceAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                            RestTemplateAutoConfiguration.class,
                            ClientTokenParserAutoConfiguration.class,
                            ClientTokenRestClientAutoConfiguration.class,
                            ClientTokenRequesterServiceAutoConfiguration.class
                    )
            )
            .withPropertyValues("yolt.client-token.requester.enabled=true")
            .withPropertyValues("service.tokens.signature-jwks=" + readSignatureJwks())
            .withConfiguration(AutoConfigurations.of(ClientTokenRequesterServiceAutoConfiguration.class));

    @SneakyThrows
    private static String readSignatureJwks() {
        return new String(readAllBytes(Paths.get("src/test/resources/secrets/signature-jwks")));
    }

    @SneakyThrows
    private static String generateSigningKeyJWK() {
        RsaJsonWebKey rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        rsaJsonWebKey.setKeyId(UUID.randomUUID().toString());
        return rsaJsonWebKey.toJson(JsonWebKey.OutputControlLevel.INCLUDE_PRIVATE);
    }

    @Test
    void createClientGatewayService() {
        runContextAndAssert(CLIENT_GATEWAY);
    }

    @Test
    void createDevPortalService() {
        runContextAndAssert(DEV_PORTAL);
    }

    @Test
    void createApiGatewayService() {
        runContextAndAssert(API_GATEWAY);
    }

    @Test
    void createSiteManagementService() {
        runContextAndAssert(SITE_MANAGEMENT);
    }

    @Test
    void createYoltAssistancePortalService() {
        runContextAndAssert(YOLT_ASSISTANCE_PORTAL);
    }

    @Test
    void createConsentStarterService() {
        runContextAndAssert(CONSENT_STARTER);
    }

    private void runContextAndAssert(String beanName) {
        contextRunner
                .withPropertyValues("yolt.client-token.requester.signing-keys." + beanName + "=" + generateSigningKeyJWK())
                .run(c -> assertThat(c).hasBean(beanName));
    }
}
