package carwash.exception;

// нарушено бизнес-правило (например, запись без гос. номера)
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
