package com.yolt.service.starter;

import com.yolt.service.starter.vault.YoltVaultCredentialsReader;
import com.yolt.service.starter.vault.YoltVaultProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;

import java.nio.file.Path;
import java.util.Properties;

@ConditionalOnClass(AwsCredentialsProvider.class)
@ConditionalOnProperty(name = "yolt.vault.aws.enabled", havingValue = "true")
@AutoConfiguration
@EnableConfigurationProperties(YoltVaultProperties.class)
@RequiredArgsConstructor
@Slf4j
public class YoltAWSAutoConfiguration {

    private final YoltVaultProperties vaultProperties;

    @Bean
    public AwsCredentialsProvider awsCredentialsFromVaultProvider() {
        Path vaultCredsFile = vaultProperties.getAws().getVaultCredsFile();
        log.info("Configuring {} with credentials from: {}", AWSCredentialsFromVaultProvider.class.getSimpleName(), vaultCredsFile);
        return new AWSCredentialsFromVaultProvider(vaultCredsFile);
    }

    @RequiredArgsConstructor
    public static class AWSCredentialsFromVaultProvider implements AwsCredentialsProvider {

        private final Path vaultCredentialsFile;

        @Override
        public AwsCredentials resolveCredentials() {
            Properties credentials = YoltVaultCredentialsReader.readCredentials(vaultCredentialsFile);

            String accessKey = credentials.getProperty("aws_access_key_id");
            String secretKey = credentials.getProperty("aws_secret_access_key");
            String sessionToken = credentials.getProperty("aws_session_token");

            return AwsSessionCredentials.create(accessKey, secretKey, sessionToken);
        }
    }
}
