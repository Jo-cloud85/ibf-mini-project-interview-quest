package ibf_tfip.mini_project.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class FirebaseAuthException extends ResponseStatusException {
    public FirebaseAuthException() {
        super(HttpStatus.FORBIDDEN,"User is not authenticated or token is invalid");
    }

    public FirebaseAuthException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
