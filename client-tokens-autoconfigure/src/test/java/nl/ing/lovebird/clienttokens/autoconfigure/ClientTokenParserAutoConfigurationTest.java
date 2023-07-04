package nl.ing.lovebird.clienttokens.autoconfigure;

import nl.ing.lovebird.clienttokens.verification.ClientTokenParser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ClientTokenParserAutoConfigurationTest {

    public static final String JWKS_JSON = "{\n" +
            "  \"keys\": [\n" +
            "    {\n" +
            "      \"kty\": \"RSA\",\n" +
            "      \"e\": \"AQAB\",\n" +
            "      \"use\": \"sig\",\n" +
            "      \"kid\": \"0d257f29-dbbf-4fa3-a602-6eb0d498be0e\",\n" +
            "      \"alg\": \"RS256\",\n" +
            "      \"n\": \"u5ORko1Zj7CA-U4EQh70jagipYx_btvVnQu4K9fY3JRar-ETnaHFhuIaIf3ZktU34JQQ5ZnAHsPPhuazMufWEMYg8VBeNkwDwG8GnoJDUFgX83L8eXQLfl2eGrwCIXvLZ2rRukWs3xxpaYFfYvzJIL4BXcec8M7wCGu7od52kCMN9Axq-LmS_g3lD_JHezFogbMigD0N1dx75OxhCr4kpSEx2BUt3PX9txg_ucTlZ-wTriFKfHeFxJXYyN49aE5gkZzRDjdLNnXCpxrU4-oj7V5tHyEHfDJMYltYQcee-YAP4bLRCkB-OQ5hijGkg-ay3aAvk-O1vZ0N46BHRkiBxQ\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ClientTokenParserAutoConfiguration.class));

    @Test
    void beanIsIgnoredWhenClientTokenParserIsNotPresent() {
        this.contextRunner
                .withClassLoader(new FilteredClassLoader(ClientTokenParser.class))
                .run((context) -> assertThat(context).doesNotHaveBean(ClientTokenParser.class));
    }

    @Test
    void beanIsIgnoredWhenOnlyClassIsPresent() {
        this.contextRunner
                .run((context) -> assertThat(context).doesNotHaveBean(ClientTokenParser.class));
    }

    @Test
    void manuallyDefinedClientTokenParserTakesPrecedence() {
        ClientTokenParser customClientTokenParser = new ClientTokenParser(JWKS_JSON);
        this.contextRunner
                .withBean(ClientTokenParser.class, () -> customClientTokenParser)
                .run((context) -> {
                    ClientTokenParser bean = context.getBean(ClientTokenParser.class);
                    assertThat(bean).isSameAs(customClientTokenParser);
                });
    }

    @Test
    void beanIsIgnoredWhenOnlyPropertyIsPresent() {
        this.contextRunner
                .withClassLoader(new FilteredClassLoader(ClientTokenParser.class))
                .withPropertyValues("service.tokens.signature-jwks=test")
                .run((context) -> assertThat(context).doesNotHaveBean(ClientTokenParser.class));
    }

    @Test
    void beanAvailable() {
        this.contextRunner
                .withPropertyValues("service.tokens.signature-jwks=" + JWKS_JSON)
                .run((context) -> assertThat(context).hasSingleBean(ClientTokenParser.class));

    }

}