package com.yolt.service.starter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class YoltAWSAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(YoltAWSAutoConfiguration.class));

    @Test
    @DisplayName("[SHOULD NOT] create autoconfiguration bean [GIVEN] vault is disabled")
    void notEnabledShouldNotHaveBeanInContext() {
        contextRunner
                .withPropertyValues("yolt.vault.enabled=false")
                .run(
                        context -> assertThat(context).doesNotHaveBean(YoltAWSAutoConfiguration.class)
                );
    }

    @Test
    @DisplayName("[SHOULD] create autoconfiguration bean [GIVEN] vault is enabled")
    void enabledShouldHaveBeanInContext(@TempDir Path tempDir) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("aws_access_key_id", "key_id");
        properties.setProperty("aws_secret_access_key", "access_key");
        properties.setProperty("aws_session_token", "session_token");

        File vaultCredsFile = tempDir.resolve("vaultCredsFile").toFile();

        properties.store(new FileWriter(vaultCredsFile), "store to properties file");

        contextRunner
                .withPropertyValues("yolt.vault.aws.enabled=true")
                .withPropertyValues("yolt.vault.aws.vaultCredsFile=" + vaultCredsFile.toPath() )
                .run(context -> {
                    assertThat(context).hasSingleBean(YoltAWSAutoConfiguration.class);
                    assertThat(context).hasSingleBean(YoltAWSAutoConfiguration.AWSCredentialsFromVaultProvider.class);

                    assertThat(context.getBean(YoltAWSAutoConfiguration.AWSCredentialsFromVaultProvider.class).resolveCredentials().accessKeyId())
                            .isEqualTo(properties.getProperty("aws_access_key_id"));
                    assertThat(context.getBean(YoltAWSAutoConfiguration.AWSCredentialsFromVaultProvider.class).resolveCredentials().secretAccessKey())
                            .isEqualTo(properties.getProperty("aws_secret_access_key"));
                });
    }

    @Test
    @DisplayName("[SHOULD] not create bean[GIVEN] vault is enabled but AwsCredentialsProvider is not on the classpath")
    void shouldNotCreateBeanIfAwsCredentialsProviderNotOnTheClassPath() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(AwsCredentialsProvider.class))
                .withPropertyValues("yolt.vault.aws.enabled=true")
                .run(
                        context -> assertThat(context).doesNotHaveBean(YoltAWSAutoConfiguration.class)
                );
    }
}