package com.yolt.sample.app;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.errorhandling.ErrorDTO;
import nl.ing.lovebird.errorhandling.ErrorInfo;
import nl.ing.lovebird.errorhandling.ExceptionHandlingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlers {

    private final ExceptionHandlingService service;

    @ExceptionHandler(TeapotException.class)
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    @ResponseBody
    public ErrorDTO handle(TeapotException e) {
        return service.logAndConstruct(ErrorConstants.CONSTANT, e);
    }

    @RequiredArgsConstructor
    @Getter
    enum ErrorConstants implements ErrorInfo {
        CONSTANT("hello coffee drinker", "1008");

        final String message;
        final String code;
    }
}

