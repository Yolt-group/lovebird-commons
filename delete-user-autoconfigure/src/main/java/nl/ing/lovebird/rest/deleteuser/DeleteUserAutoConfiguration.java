package nl.ing.lovebird.rest.deleteuser;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.REACTIVE;
import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

@AutoConfiguration
@ConditionalOnClass(UserDeleter.class)
public class DeleteUserAutoConfiguration {

    @Bean
    public UserDeleter userDeleter() {
        return new UserDeleter();
    }

    @Bean
    @ConditionalOnWebApplication(type = SERVLET)
    public DeleteUserController deleteUserController(UserDeleter userDeleter) {
        return new DeleteUserController(userDeleter);
    }

    @Bean
    @ConditionalOnWebApplication(type = REACTIVE)
    public ReactiveDeleteUserController reactiveDeleteUserController(UserDeleter userDeleter) {
        return new ReactiveDeleteUserController(userDeleter);
    }

}
