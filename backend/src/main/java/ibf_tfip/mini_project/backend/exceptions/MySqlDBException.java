package ibf_tfip.mini_project.backend.exceptions;

public class MySqlDBException extends RuntimeException {
    public MySqlDBException() {
        super("Failed to save to MySql.");
    }

    public MySqlDBException(String message) {
        super(message);
    }
}
