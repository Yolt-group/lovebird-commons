package nl.ing.lovebird.clienttokens.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static nl.ing.lovebird.clienttokens.autoconfigure.ClientTokenParserAutoConfigurationTest.JWKS_JSON;
import static org.assertj.core.api.Assertions.assertThat;

class ClientTokenConvertersAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ClientTokenConverterAutoConfiguration.class,
                    ClientTokenParserAutoConfiguration.class
            ))
            .withPropertyValues(
                    "service.tokens.signature-jwks=" + JWKS_JSON
            );

    @Test
    void testClientTokenConvertersAvailable() {
        contextRunner
                .run(context -> assertThat(context)
                        .hasBean("clientTokenHeaderFromBytesConverter")
                        .hasBean("clientTokenHeaderFromStringConverter")
                        .hasBean("clientGroupTokenHeaderFromBytesConverter")
                        .hasBean("clientGroupTokenHeaderFromStringConverter")
                );
    }


}
