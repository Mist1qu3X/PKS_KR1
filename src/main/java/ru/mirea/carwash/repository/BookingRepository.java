package ru.mirea.carwash.repository;

import ru.mirea.carwash.exception.DatabaseException;
import ru.mirea.carwash.model.Booking;
import ru.mirea.carwash.model.BookingStatus;
import ru.mirea.carwash.model.ServiceType;
import ru.mirea.carwash.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// работа с таблицей bookings через JDBC
public class BookingRepository implements Repository<Booking> {

    // общий SELECT: сама запись + имя клиента через JOIN
    private static final String BASE_SELECT =
            "SELECT b.id, b.client_id, b.car_number, b.car_model, b.service_type, "
                    + "b.status, b.scheduled_at, b.price, b.created_at, c.full_name AS client_name "
                    + "FROM bookings b JOIN clients c ON b.client_id = c.id ";

    @Override
    public Booking save(Booking booking) {
        String sql = "INSERT INTO bookings "
                + "(client_id, car_number, car_model, service_type, status, scheduled_at, price) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, booking.getClientId());
            statement.setString(2, booking.getCarNumber());
            statement.setString(3, booking.getCarModel());
            statement.setString(4, booking.getServiceType().name());
            statement.setString(5, booking.getStatus().name());
            statement.setTimestamp(6, Timestamp.valueOf(booking.getScheduledAt()));
            statement.setDouble(7, booking.getPrice());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    booking.setId(keys.getInt(1));
                }
            }
            return booking;
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при сохранении записи.", e);
        }
    }

    @Override
    public List<Booking> findAll() {
        return queryList(BASE_SELECT + "ORDER BY b.scheduled_at");
    }

    @Override
    public Optional<Booking> findById(int id) {
        String sql = BASE_SELECT + "WHERE b.id = ?";
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
            throw new DatabaseException("Ошибка при поиске записи по ID.", e);
        }
    }

    @Override
    public void update(Booking booking) {
        String sql = "UPDATE bookings SET client_id = ?, car_number = ?, car_model = ?, "
                + "service_type = ?, status = ?, scheduled_at = ?, price = ? WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, booking.getClientId());
            statement.setString(2, booking.getCarNumber());
            statement.setString(3, booking.getCarModel());
            statement.setString(4, booking.getServiceType().name());
            statement.setString(5, booking.getStatus().name());
            statement.setTimestamp(6, Timestamp.valueOf(booking.getScheduledAt()));
            statement.setDouble(7, booking.getPrice());
            statement.setInt(8, booking.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при обновлении записи.", e);
        }
    }

    @Override
    public void deleteById(int id) {
        String sql = "DELETE FROM bookings WHERE id = ?";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при удалении записи.", e);
        }
    }

    // ---------- поиск ----------

    // поиск по части гос. номера, без учёта регистра (ILIKE)
    public List<Booking> searchByCarNumber(String carNumber) {
        String sql = BASE_SELECT + "WHERE b.car_number ILIKE ? ORDER BY b.scheduled_at";
        return queryListWithString(sql, "%" + carNumber + "%");
    }

    // поиск по части имени клиента
    public List<Booking> searchByClientName(String name) {
        String sql = BASE_SELECT + "WHERE c.full_name ILIKE ? ORDER BY b.scheduled_at";
        return queryListWithString(sql, "%" + name + "%");
    }

    // ---------- фильтрация ----------

    public List<Booking> filterByStatus(BookingStatus status) {
        String sql = BASE_SELECT + "WHERE b.status = ? ORDER BY b.scheduled_at";
        return queryListWithString(sql, status.name());
    }

    public List<Booking> filterByServiceType(ServiceType type) {
        String sql = BASE_SELECT + "WHERE b.service_type = ? ORDER BY b.scheduled_at";
        return queryListWithString(sql, type.name());
    }

    public List<Booking> filterByDateRange(LocalDateTime from, LocalDateTime to) {
        String sql = BASE_SELECT + "WHERE b.scheduled_at BETWEEN ? AND ? ORDER BY b.scheduled_at";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setTimestamp(1, Timestamp.valueOf(from));
            statement.setTimestamp(2, Timestamp.valueOf(to));
            return extract(statement);
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при фильтрации записей по датам.", e);
        }
    }

    // ---------- вспомогательные методы ----------

    private List<Booking> queryList(String sql) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            return extract(statement);
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при получении списка записей.", e);
        }
    }

    private List<Booking> queryListWithString(String sql, String param) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, param);
            return extract(statement);
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка при выполнении запроса.", e);
        }
    }

    private List<Booking> extract(PreparedStatement statement) throws SQLException {
        List<Booking> result = new ArrayList<>();
        try (ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    // строка результата -> объект Booking
    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking booking = new Booking(
                rs.getInt("id"),
                rs.getInt("client_id"),
                rs.getString("car_number"),
                rs.getString("car_model"),
                ServiceType.valueOf(rs.getString("service_type")),
                BookingStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("scheduled_at").toLocalDateTime(),
                rs.getDouble("price"),
                rs.getTimestamp("created_at").toLocalDateTime());
        booking.setClientName(rs.getString("client_name"));
        return booking;
    }
}
