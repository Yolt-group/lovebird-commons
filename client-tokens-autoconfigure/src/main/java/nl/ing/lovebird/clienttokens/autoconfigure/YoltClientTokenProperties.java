package nl.ing.lovebird.clienttokens.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("yolt.client-token")
@Data
public class YoltClientTokenProperties {

    private Requester requester;

    @Data
    public static class Requester {

        public static final String SIGNATURE_PRIVATE_KEY_PREFIX = "yolt.client-token.requester.signing-keys.";

        public static final String CLIENT_GATEWAY = "client-gateway";
        public static final String DEV_PORTAL = "dev-portal";
        public static final String API_GATEWAY = "api-gateway";
        public static final String SITE_MANAGEMENT = "site-management";
        public static final String YOLT_ASSISTANCE_PORTAL = "yolt-assistance-portal";
        public static final String CONSENT_STARTER = "consent-starter";

        private boolean enabled;
        private SigningKeys signingKeys = new SigningKeys();

        @Data
        public static class SigningKeys {
            private String clientGateway;
            private String devPortal;
            private String apiGateway;
            private String siteManagement;
            private String yoltAssistancePortal;
            private String consentStarter;
        }
    }
}

