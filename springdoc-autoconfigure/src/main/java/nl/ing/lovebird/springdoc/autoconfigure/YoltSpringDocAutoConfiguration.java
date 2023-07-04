package nl.ing.lovebird.springdoc.autoconfigure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.springdoc.ExposeExternalApiPlugin;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
@ConditionalOnClass(OperationCustomizer.class)
public class YoltSpringDocAutoConfiguration {
    @Bean
    public OperationCustomizer exposeExternalApiPlugin() {
        return new ExposeExternalApiPlugin();
    }
}
