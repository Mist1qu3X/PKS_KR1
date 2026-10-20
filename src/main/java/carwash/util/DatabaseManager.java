package carwash.util;

import carwash.exception.DatabaseException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

// подключение к базе PostgreSQL.
// Параметры читаются из файла ресурсов database.properties,
// чтобы не хранить пароль прямо в исходном коде.
public class DatabaseManager {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    // загружаем настройки один раз при старте приложения
    static {
        Properties props = new Properties();
        try (InputStream in = DatabaseManager.class.getResourceAsStream("/database.properties")) {
            if (in == null) {
                throw new DatabaseException(
                        "Файл database.properties не найден в ресурсах проекта.", null);
            }
            props.load(in);
        } catch (IOException e) {
            throw new DatabaseException("Не удалось прочитать database.properties.", e);
        }
        URL = props.getProperty("db.url");
        USER = props.getProperty("db.user");
        PASSWORD = props.getProperty("db.password");
    }

    private DatabaseManager() {
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Не удалось подключиться к базе данных. Проверьте, что PostgreSQL "
                            + "запущен и параметры в database.properties указаны верно.", e);
        }
    }
}
