package nl.ing.lovebird.clienttokens.verification;

import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.clienttokens.ClientToken;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
public class ClientTokenHeaderFromStringConverter implements Converter<String, ClientToken> {

    private final ClientTokenParser parser;

    @Override
    public ClientToken convert(final String source) {
        return (ClientToken) parser.parseClientToken(source);
    }
}
