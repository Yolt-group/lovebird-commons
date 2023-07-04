package nl.ing.lovebird.clienttokens.autoconfigure;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("service.tokens")
@Data
public class ServiceTokenProperties {

    // **
    // DO NOT add more properties under service.tokens.
    // This property tree has grown organically and is a bit of a mess.
    // Use YoltClientTokenProperties instead.
    // **
    private String signatureJwks;
    private String url;
}
