package carwash.util;

import carwash.exception.DatabaseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

// пересоздаёт таблицы и заливает тестовые данные из schema.sql
public class DatabaseInitializer {

    private static final String SCHEMA_FILE = "/schema.sql";

    private DatabaseInitializer() {
    }

    public static void initialize() {
        String script = readScript();
        // команды в файле разделены символом ';'
        String[] statements = script.split(";");

        // тут обычный Statement, потому что это DDL из файла без параметров
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {

            for (String sql : statements) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    statement.execute(trimmed);
                }
            }
            System.out.println("База данных успешно инициализирована.");
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при инициализации базы данных.", e);
        }
    }

    // читаем файл schema.sql из ресурсов
    private static String readScript() {
        try (InputStream in = DatabaseInitializer.class.getResourceAsStream(SCHEMA_FILE)) {
            if (in == null) {
                throw new DatabaseException("Файл schema.sql не найден в ресурсах.", null);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new DatabaseException("Не удалось прочитать файл schema.sql.", e);
        }
    }
}
