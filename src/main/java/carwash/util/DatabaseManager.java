package carwash.util;

import carwash.exception.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// подключение к базе PostgreSQL
public class DatabaseManager {

    // укажите параметры своей базы PostgreSQL
    private static final String URL = "jdbc:postgresql://localhost:5432/carwash";
    private static final String USER = "postgres";
    private static final String PASSWORD = "your_password";

    private DatabaseManager() {
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Не удалось подключиться к базе данных. Проверьте, что PostgreSQL "
                            + "запущен и параметры подключения указаны верно.", e);
        }
    }
}
