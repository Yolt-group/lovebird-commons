package nl.ing.lovebird.clienttokens.autoconfigure;

import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.clienttokens.ClientGroupToken;
import nl.ing.lovebird.clienttokens.ClientToken;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.verification.ClientGroupTokenHeaderFromBytesConverter;
import nl.ing.lovebird.clienttokens.verification.ClientGroupTokenHeaderFromStringConverter;
import nl.ing.lovebird.clienttokens.verification.ClientTokenHeaderFromBytesConverter;
import nl.ing.lovebird.clienttokens.verification.ClientTokenHeaderFromStringConverter;
import nl.ing.lovebird.clienttokens.verification.ClientTokenParser;
import nl.ing.lovebird.clienttokens.verification.ClientUserTokenHeaderFromBytesConverter;
import nl.ing.lovebird.clienttokens.verification.ClientUserTokenHeaderFromStringConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;

@AutoConfiguration
@ConditionalOnClass(ClientTokenParser.class)
@RequiredArgsConstructor
public class ClientTokenConverterAutoConfiguration {

    private final ClientTokenParser parser;

    @Bean
    public Converter<byte[], ClientUserToken> clientUserTokenHeaderFromBytesConverter() {
        return new ClientUserTokenHeaderFromBytesConverter(parser);
    }

    @Bean
    public Converter<String, ClientUserToken> clientUserTokenHeaderFromStringConverter() {
        return new ClientUserTokenHeaderFromStringConverter(parser);
    }

    @Bean
    public Converter<byte[], ClientToken> clientTokenHeaderFromBytesConverter() {
        return new ClientTokenHeaderFromBytesConverter(parser);
    }

    @Bean
    public Converter<String, ClientToken> clientTokenHeaderFromStringConverter() {
        return new ClientTokenHeaderFromStringConverter(parser);
    }

    @Bean
    public Converter<byte[], ClientGroupToken> clientGroupTokenHeaderFromBytesConverter() {
        return new ClientGroupTokenHeaderFromBytesConverter(parser);
    }

    @Bean
    public Converter<String, ClientGroupToken> clientGroupTokenHeaderFromStringConverter() {
        return new ClientGroupTokenHeaderFromStringConverter(parser);
    }
}
