package nl.ing.lovebird.clienttokens.autoconfigure.test;

import nl.ing.lovebird.clienttokens.autoconfigure.ClientTokenParserAutoConfiguration;
import nl.ing.lovebird.clienttokens.test.TestClientTokens;
import nl.ing.lovebird.clienttokens.verification.ClientTokenParser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ClientTokenTestParserAutoConfigurationTest {

    final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ClientTokenParserAutoConfiguration.class))
            .withConfiguration(AutoConfigurations.of(ClientTokenTestParserAutoConfiguration.class));

    @Test
    void configIgnoredWhenClientTokenParserIsNotPresent() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(ClientTokenParser.class))
                .run((context) -> assertThat(context).doesNotHaveBean(TestClientTokens.class));
    }
    @Test
    void configIgnoredWhenTestClientTokensIsNotPresent() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(TestClientTokens.class))
                .run((context) -> assertThat(context).doesNotHaveBean("testClientTokenParser"));
    }

    @Test
    void createsTestClientTokensAndTestParserBeans() {
        contextRunner
                .run((context) -> assertThat(context)
                        .hasSingleBean(TestClientTokens.class)
                        .hasSingleBean(ClientTokenParser.class)
                        .hasBean("testClientTokenParser")
                );
    }
}