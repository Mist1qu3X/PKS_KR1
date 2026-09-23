package carwash.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// запись на автомойку — основная сущность
public class Booking {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private int id;
    private int clientId;
    private String carNumber;
    private String carModel;
    private ServiceType serviceType;
    private BookingStatus status;
    private LocalDateTime scheduledAt;
    private double price;
    private LocalDateTime createdAt;

    // имя клиента не хранится в таблице bookings, берём его через JOIN при чтении
    private String clientName;

    // новая запись (статус по умолчанию — CREATED)
    public Booking(int clientId, String carNumber, String carModel,
                   ServiceType serviceType, LocalDateTime scheduledAt, double price) {
        this.clientId = clientId;
        this.carNumber = carNumber;
        this.carModel = carModel;
        this.serviceType = serviceType;
        this.scheduledAt = scheduledAt;
        this.price = price;
        this.status = BookingStatus.CREATED;
    }

    // запись, прочитанная из базы
    public Booking(int id, int clientId, String carNumber, String carModel,
                   ServiceType serviceType, BookingStatus status,
                   LocalDateTime scheduledAt, double price, LocalDateTime createdAt) {
        this.id = id;
        this.clientId = clientId;
        this.carNumber = carNumber;
        this.carModel = carModel;
        this.serviceType = serviceType;
        this.status = status;
        this.scheduledAt = scheduledAt;
        this.price = price;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public String toString() {
        return String.format(
                "Запись #%d | %s (%s) | клиент: %s | %s | %s | %.2f руб. | %s",
                id,
                carNumber,
                (carModel == null || carModel.isEmpty() ? "-" : carModel),
                (clientName == null ? "id=" + clientId : clientName),
                serviceType.getTitle(),
                scheduledAt.format(DATE_FORMAT),
                price,
                status.getTitle());
    }
}
