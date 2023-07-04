package com.yolt.service.starter.vault;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.Properties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YoltVaultCredentialsReader {

    public static Properties readCredentials(Path credentialsFilePath) {
        return nl.ing.lovebird.vault.YoltVaultCredentialsReader.readCredentials(credentialsFilePath);
    }
}
