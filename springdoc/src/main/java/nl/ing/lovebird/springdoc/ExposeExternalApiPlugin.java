package nl.ing.lovebird.springdoc;


import io.swagger.v3.oas.models.Operation;
import nl.ing.lovebird.springdoc.annotations.ExternalApi;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.web.method.HandlerMethod;

public class ExposeExternalApiPlugin implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        ExternalApi annotation = handlerMethod.getMethodAnnotation(ExternalApi.class);
        if (annotation != null) {
            operation.addExtension("x-expose", "external-api");
        }

        return operation;
    }
}
