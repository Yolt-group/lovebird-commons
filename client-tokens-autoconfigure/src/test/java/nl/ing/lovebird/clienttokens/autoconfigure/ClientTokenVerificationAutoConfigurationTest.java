package nl.ing.lovebird.clienttokens.autoconfigure;

import nl.ing.lovebird.clienttokens.verification.ClientGroupIdVerificationService;
import nl.ing.lovebird.clienttokens.verification.ClientIdVerificationService;
import nl.ing.lovebird.clienttokens.web.VerifiedClientTokenParameterResolver;
import nl.ing.lovebird.errorhandling.ExceptionHandlingService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static nl.ing.lovebird.clienttokens.autoconfigure.ClientTokenParserAutoConfigurationTest.JWKS_JSON;
import static org.assertj.core.api.Assertions.assertThat;

class ClientTokenVerificationAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ClientTokenConverterAutoConfiguration.class,
                    ClientTokenParserAutoConfiguration.class,
                    ClientTokenVerificationAutoConfiguration.class
            ))
            .withBean(ExceptionHandlingService.class, () -> new ExceptionHandlingService("TEST"))
            .withPropertyValues(
                    "service.tokens.signature-jwks=" + JWKS_JSON
            );


    @Test
    void testClientTokenVerificationAvailable() {
        contextRunner
                .run(context -> assertThat(context)
                        .hasSingleBean(ClientIdVerificationService.class)
                        .hasSingleBean(ClientGroupIdVerificationService.class)
                        .hasSingleBean(VerifiedClientTokenParameterResolver.class)
                        .hasSingleBean(VerifiedClientTokenWebMvcConfigurer.class)
                );
    }
}
