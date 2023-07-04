package nl.ing.lovebird.clienttokens.verification;

import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.clienttokens.ClientToken;
import org.springframework.core.convert.converter.Converter;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class ClientTokenHeaderFromBytesConverter implements Converter<byte[], ClientToken> {

    private final ClientTokenParser parser;

    @Override
    public ClientToken convert(final byte[] source) {
        // Handle JSON encoded strings. Remove when all pods on > 13.0.25
        // See: https://yolt.atlassian.net/browse/CHAP-145
        String clientToken = new String(source, StandardCharsets.UTF_8);
        if (clientToken.length() > 1 && clientToken.startsWith("\"") && clientToken.endsWith("\"")) {
            clientToken = clientToken.substring(1, clientToken.length() - 1);
        }
        return (ClientToken) parser.parseClientToken(clientToken);
    }
}
