package nl.ing.lovebird.clienttokens.verification;

import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
public class ClientUserTokenHeaderFromStringConverter implements Converter<String, ClientUserToken> {

    private final ClientTokenParser parser;

    @Override
    public ClientUserToken convert(final String source) {
        return (ClientUserToken) parser.parseClientToken(source);
    }
}
