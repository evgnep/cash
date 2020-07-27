package su.nepom.cash.server.remote.crud;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public class EnityAlreadyExistsExeption extends RuntimeException {
}
