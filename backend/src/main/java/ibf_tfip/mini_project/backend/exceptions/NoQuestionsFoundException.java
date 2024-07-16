package ibf_tfip.mini_project.backend.exceptions;

public class NoQuestionsFoundException extends RuntimeException {
    public NoQuestionsFoundException() {
        super("Failed to retrieve questions. No questions found.");
    }

    public NoQuestionsFoundException(String message) {
        super(message);
    }
}
