package ibf_tfip.mini_project.backend.exceptions;

public class NoSubThreadFoundException extends RuntimeException {
    public NoSubThreadFoundException() {
        super("Failed to retrieve sub-thread. No sub-thread found.");
    }

    public NoSubThreadFoundException(String message) {
        super(message);
    }
}
