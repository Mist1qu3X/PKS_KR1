package ru.mirea.carwash.exception;

// запись или клиент с таким id не найдены
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
