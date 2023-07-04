package nl.ing.lovebird.clienttokens.autoconfigure;

import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.clienttokens.web.VerifiedClientTokenParameterResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class VerifiedClientTokenWebMvcConfigurer implements WebMvcConfigurer {

    private final VerifiedClientTokenParameterResolver verifiedClientTokenParameterResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(verifiedClientTokenParameterResolver);
    }

}
