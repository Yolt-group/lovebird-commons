package nl.ing.lovebird.errorhandling;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class ErrorResponse {

    private final ErrorInfo errorInfo;
    private final HttpStatus httpStatus;
    private Level logLevel = Level.ERROR;

}
