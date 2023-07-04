package nl.ing.lovebird.clienttokens.autoconfigure;

import nl.ing.lovebird.secretspipeline.VaultKeys;
import nl.ing.lovebird.secretspipeline.VaultKeysReader;
import nl.ing.lovebird.secretspipeline.converters.JsonWebKeysStoreReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import static java.nio.file.Files.readAllBytes;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class ClientTokenRequesterVaultAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues("spring.application.name=test_app")
            .withConfiguration(AutoConfigurations.of(
                    RestTemplateAutoConfiguration.class,
                    ClientTokenRestClientAutoConfiguration.class,
                    ClientTokenParserAutoConfiguration.class,
                    ClientTokenRequesterVaultAutoConfiguration.class
            ));

    @Test
    @DisplayName("[SHOULD NOT] create autoconfiguration bean [GIVEN] vault based secret is disabled")
    void notEnabledShouldNotHaveBeanInContext() {
        contextRunner
                .withPropertyValues("yolt.client-token.requester.vault-based-secret.enabled=false")
                .run(c -> assertThat(c)
                        .doesNotHaveBean("vaultClientTokenRequester"));
    }

    @Test
    @DisplayName("[SHOULD] create autoconfiguration bean and token requester service[GIVEN] vault based secret is enabled")
    void enabledShouldHaveBeanInContext() throws IOException {
        JsonWebKeysStoreReader jsonWebKeysStoreReader = new JsonWebKeysStoreReader();
        VaultKeysReader vaultKeysReader = new VaultKeysReader(singletonList(jsonWebKeysStoreReader));
        URI secretsLocation = new File("src/test/resources/secrets").toURI();
        VaultKeys vaultKeys = vaultKeysReader.readFiles(secretsLocation);
        contextRunner
                .withBean(VaultKeys.class, () -> vaultKeys)
                .withPropertyValues("yolt.client-token.requester.vault-based-secret.enabled=true")
                .withPropertyValues("service.tokens.signature-jwks=" + new String(readAllBytes(Paths.get("src/test/resources/secrets/signature-jwks"))))
                .run(c -> assertThat(c).hasBean("vaultClientTokenRequester"));
    }
}