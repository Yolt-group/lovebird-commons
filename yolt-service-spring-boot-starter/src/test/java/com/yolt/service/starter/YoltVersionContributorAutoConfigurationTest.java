package com.yolt.service.starter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.core.env.PropertySource;

import static org.assertj.core.api.Assertions.assertThat;

class YoltVersionContributorAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    YoltVersionContributorAutoConfiguration.class
            ));

    @Test
    void testVersionsContributor() {
        contextRunner.run(context -> {
            assertThat(context.getEnvironment().getPropertySources())
                    .extracting(PropertySource::getName)
                    .contains("class path resource [application-versions.properties]");

            String springBootVersion = context.getEnvironment().getProperty("info.spring-boot");
            assertThat(springBootVersion).startsWith("2."); // Good enough until v3
        });
    }
}
