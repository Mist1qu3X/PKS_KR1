package ru.mirea.carwash.repository;

import java.util.List;
import java.util.Optional;

// общий интерфейс репозитория (CRUD), реализуют ClientRepository и BookingRepository
public interface Repository<T> {

    T save(T entity);

    List<T> findAll();

    Optional<T> findById(int id);

    void update(T entity);

    void deleteById(int id);
}
