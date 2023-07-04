package nl.ing.lovebird.clienttokens.autoconfigure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.verification.ClientGroupIdVerificationService;
import nl.ing.lovebird.clienttokens.verification.ClientIdVerificationService;
import nl.ing.lovebird.clienttokens.verification.ClientTokenParser;
import nl.ing.lovebird.clienttokens.verification.ClientUserIdVerificationService;
import nl.ing.lovebird.clienttokens.verification.UserIdVerificationService;
import nl.ing.lovebird.clienttokens.web.ClientTokenVerificationExceptionHandlers;
import nl.ing.lovebird.clienttokens.web.VerifiedClientTokenParameterResolver;
import nl.ing.lovebird.errorhandling.ExceptionHandlingService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

@AutoConfiguration
@ConditionalOnClass(VerifiedClientTokenParameterResolver.class)
@Slf4j
@RequiredArgsConstructor
public class ClientTokenVerificationAutoConfiguration {

    private final ClientTokenParser parser;

    @Bean
    public ClientUserIdVerificationService createClientUserIdVerificationService() {
        return new ClientUserIdVerificationService();
    }

    @Bean
    public UserIdVerificationService createUserIdVerificationService() {
        return new UserIdVerificationService();
    }

    @Bean
    public ClientIdVerificationService createClientIdVerificationService() {
        return new ClientIdVerificationService();
    }

    @Bean
    public ClientGroupIdVerificationService createClientGroupIdVerificationService() {
        return new ClientGroupIdVerificationService();
    }

    @Bean
    public VerifiedClientTokenParameterResolver createVerifiedClientTokenParameterResolver(
            ClientIdVerificationService clientIdVerificationService,
            ClientUserIdVerificationService clientUserIdVerificationService,
            UserIdVerificationService userIdVerificationService) {
        return new VerifiedClientTokenParameterResolver(parser, clientIdVerificationService, clientUserIdVerificationService, userIdVerificationService);
    }

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE - 20) // Higher priority then the controller advice from error-handling
    @ConditionalOnWebApplication
    public ClientTokenVerificationExceptionHandlers clientTokenVerificationExceptionHandlers(ExceptionHandlingService service) {
        return new ClientTokenVerificationExceptionHandlers(service);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = SERVLET)
    @Import(VerifiedClientTokenWebMvcConfigurer.class)
    public static class ConfigureWebMvc {

    }
}
