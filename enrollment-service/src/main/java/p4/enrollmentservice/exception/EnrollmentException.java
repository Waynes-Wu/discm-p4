package p4.enrollmentservice.exception;

public class EnrollmentException extends RuntimeException {
    public EnrollmentException(String message) {
        super(message);
    }

    public EnrollmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
