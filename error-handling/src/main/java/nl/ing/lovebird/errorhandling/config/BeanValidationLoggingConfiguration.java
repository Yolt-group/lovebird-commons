package nl.ing.lovebird.errorhandling.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.CustomValidatorBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.validation.ConstraintViolation;

@Configuration(proxyBeanMethods = false)
@Order(1)
public class BeanValidationLoggingConfiguration implements WebMvcConfigurer {

    /**
     * Returns a custom {@see SpringValidatorAdapter} which redacts the `rejectedValue` from any
     * validation constraint violation message.
     *
     * @return a {@see SpringValidatorAdapter}
     */
    @Override
    public Validator getValidator() {
        return new CustomValidatorBean() {

            @Override
            protected Object getRejectedValue(@NonNull String field, ConstraintViolation<Object> violation, @NonNull BindingResult bindingResult) {
                return "rejectedValue has been redacted.";
            }
        };
    }
}
