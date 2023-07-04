package nl.ing.lovebird.rest.deleteuser;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.clienttokens.ClientUserToken;
import nl.ing.lovebird.clienttokens.annotations.VerifiedClientToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Callable;

import static nl.ing.lovebird.rest.deleteuser.ReactiveDeleteUserController.CONTROLLER_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(CONTROLLER_PATH)
@Slf4j
public class ReactiveDeleteUserController {
    public static final String CONTROLLER_PATH = "/delete-user";
    public static final String ID_PATH = "/{userId}";

    private final UserDeleter deleter;

    @Autowired
    public ReactiveDeleteUserController(final UserDeleter service) {
        this.deleter = service;
    }

    /**
     * @return {@link Callable} returning a {@link ResponseEntity} with a {@link DeleteUserResult} in the body
     */
    @DeleteMapping(value = ID_PATH, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<ResponseEntity<Void>> deleteUser(@Parameter(hidden = true) @VerifiedClientToken final ClientUserToken clientUserToken) {
        log.info("Deleting all data for user with id {}.", clientUserToken.getUserIdClaim());
        return Mono.fromCallable(() -> mapDeleteUserResponse(deleter.executeDeleteUser(clientUserToken)))
                .subscribeOn(Schedulers.boundedElastic());

    }

    private ResponseEntity<Void> mapDeleteUserResponse(final DeleteUserResult result) {
        if (result.isSuccess()) {
            return ResponseEntity.noContent().build();
        } else {
            throw DeleteUserException.of(result);
        }
    }
}
