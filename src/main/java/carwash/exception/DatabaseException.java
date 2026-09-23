package carwash.exception;

// ошибка при работе с базой (оборачиваем SQLException)
public class DatabaseException extends RuntimeException {
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
