package com.yolt.service.starter.vault;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@ConfigurationProperties("yolt.vault")
public class YoltVaultProperties {

    private boolean enabled;
    private Secret secret = new Secret();
    private Secrets secrets = new Secrets();
    private Aws aws = new Aws();

    @Data
    public static class Secret {
        private Resource location = new DefaultResourceLoader().getResource("file:/vault/secrets");
        private String description;
        private boolean enabled;
    }

    @Data
    public static class Https {
        private boolean enabled;
    }

    @Data
    public static class Secrets {
        private String directory = "/vault/secrets/";
        private Tls tls = new Tls();

        @Data
        public static class Tls {
            private String certFileName = "cert";
            private String issuingCaFileName = "issuing_ca";
            private String privateKeyFileName = "private_key";
        }
    }


    @Data
    public static class Aws {
        private boolean enabled;
        private Path vaultCredsFile = Paths.get("/vault/secrets/aws");
    }

    @Data
    public static class Kafka {
        private boolean enabled;
    }
}

