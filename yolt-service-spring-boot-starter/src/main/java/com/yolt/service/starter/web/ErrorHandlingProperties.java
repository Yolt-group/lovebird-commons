package com.yolt.service.starter.web;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;

@ConfigurationProperties("yolt.commons.error-handling")
@Data
public class ErrorHandlingProperties {

    @NotBlank
    private String prefix;
}
