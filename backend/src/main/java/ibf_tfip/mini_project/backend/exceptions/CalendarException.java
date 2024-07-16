package ibf_tfip.mini_project.backend.exceptions;

public class CalendarException extends RuntimeException {
    public CalendarException() {
        super("Failed to connect to Google Calendar...");
    }

    public CalendarException(String message) {
        super(message);
    }
}
