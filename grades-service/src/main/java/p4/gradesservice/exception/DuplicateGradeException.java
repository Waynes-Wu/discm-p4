package p4.gradesservice.exception;

public class DuplicateGradeException extends RuntimeException {
    public DuplicateGradeException(String message) {
        super(message);
    }
}