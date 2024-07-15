package ibf_tfip.mini_project.backend.exceptions;

public class EmailException extends RuntimeException {
    public EmailException() {
        super("Failed to send email for custom job");
    }

    public EmailException(String message) {
        super(message);
    }
}
