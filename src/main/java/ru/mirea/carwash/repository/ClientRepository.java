package ru.mirea.carwash.repository;

import ru.mirea.carwash.exception.DatabaseException;
import ru.mirea.carwash.model.Client;
import ru.mirea.carwash.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// работа с таблицей clients через JDBC (везде PreparedStatement с параметрами)
public class ClientRepository implements Repository<Client> {

    @Override
    public Client save(Client client) {
        String sql = "INSERT INTO clients (full_name, phone, email) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, client.getFullName());
            statement.setString(2, client.getPhone());
            statement.setString(3, client.getEmail());
            statement.executeUpdate();

            // забираем сгенерированный базой id
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    client.setId(keys.getInt(1));
                }
            }
            return client;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при сохранении клиента.", e);
        }
    }

    @Override
    public List<Client> findAll() {
        String sql = "SELECT id, full_name, phone, email, created_at FROM clients ORDER BY id";
        List<Client> result = new ArrayList<>();
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                result.add(mapRow(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при получении списка клиентов.", e);
        }
    }

    @Override
    public Optional<Client> findById(int id) {
        String sql = "SELECT id, full_name, phone, email, created_at FROM clients WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при поиске клиента по ID.", e);
        }
    }

    // нужен для проверки уникальности телефона
    public Optional<Client> findByPhone(String phone) {
        String sql = "SELECT id, full_name, phone, email, created_at FROM clients WHERE phone = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, phone);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при поиске клиента по телефону.", e);
        }
    }

    @Override
    public void update(Client client) {
        String sql = "UPDATE clients SET full_name = ?, phone = ?, email = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, client.getFullName());
            statement.setString(2, client.getPhone());
            statement.setString(3, client.getEmail());
            statement.setInt(4, client.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при обновлении клиента.", e);
        }
    }

    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM clients WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException(
                    "Ошибка при удалении клиента. Возможно, у клиента есть записи на мойку.", e);
        }
    }

    // строка результата -> объект Client
    private Client mapRow(ResultSet rs) throws SQLException {
        Timestamp created = rs.getTimestamp("created_at");
        return new Client(
                rs.getInt("id"),
                rs.getString("full_name"),
                rs.getString("phone"),
                rs.getString("email"),
                created == null ? null : created.toLocalDateTime());
    }
}
