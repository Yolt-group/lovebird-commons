package nl.ing.lovebird.clienttokens.verification;

import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import org.springframework.core.convert.converter.Converter;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class ClientUserTokenHeaderFromBytesConverter implements Converter<byte[], ClientUserToken> {

    private final ClientTokenParser parser;

    @Override
    public ClientUserToken convert(final byte[] source) {
        // Handle JSON encoded strings. Remove when all pods on > 13.0.25
        // See: https://yolt.atlassian.net/browse/CHAP-145
        String clientToken = new String(source, StandardCharsets.UTF_8);
        if (clientToken.length() > 1 && clientToken.startsWith("\"") && clientToken.endsWith("\"")) {
            clientToken = clientToken.substring(1, clientToken.length() - 1);
        }
        return (ClientUserToken) parser.parseClientToken(clientToken);
    }
}
