package su.nepom.cash.server.cheque;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class GetChequeException extends RuntimeException {
    public GetChequeException(String message) {
        super(message);
    }

    public GetChequeException(String message, Throwable cause) {
        super(message, cause);
    }
}
