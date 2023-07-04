package nl.ing.lovebird.clienttokens.verification;

import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.clienttokens.ClientGroupToken;
import org.springframework.core.convert.converter.Converter;

@RequiredArgsConstructor
public class ClientGroupTokenHeaderFromStringConverter implements Converter<String, ClientGroupToken> {

    private final ClientTokenParser parser;

    @Override
    public ClientGroupToken convert(final String source) {
        return (ClientGroupToken) parser.parseClientToken(source);
    }
}
