package carwash.service;

import carwash.exception.BusinessException;
import carwash.exception.EntityNotFoundException;
import carwash.model.Booking;
import carwash.model.BookingStatus;
import carwash.model.ServiceType;
import carwash.repository.BookingRepository;
import carwash.repository.ClientRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

// логика работы с записями: бизнес-правила + обращение к репозиторию
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ClientRepository clientRepository;

    public BookingService(BookingRepository bookingRepository, ClientRepository clientRepository) {
        this.bookingRepository = bookingRepository;
        this.clientRepository = clientRepository;
    }

    public Booking create(int clientId, String carNumber, String carModel,
                          ServiceType serviceType, LocalDateTime scheduledAt, double price) {
        validateClientExists(clientId);
        validateCarNumber(carNumber);
        validateServiceType(serviceType);
        validateFutureDate(scheduledAt);
        validatePrice(price);

        Booking booking = new Booking(clientId, carNumber.trim(), carModel, serviceType, scheduledAt, price);
        return bookingRepository.save(booking);
    }

    public List<Booking> getAll() {
        return bookingRepository.findAll();
    }

    public Booking getById(int id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Запись с ID " + id + " не найдена."));
    }

    // изменение полей записи (без смены статуса)
    public void update(int id, String carNumber, String carModel,
                       ServiceType serviceType, LocalDateTime scheduledAt, double price) {
        Booking booking = getById(id);

        // завершённую или отменённую запись менять нельзя
        if (booking.getStatus().isFinal()) {
            throw new BusinessException(
                    "Нельзя изменить запись со статусом \"" + booking.getStatus().getTitle() + "\".");
        }

        validateCarNumber(carNumber);
        validateServiceType(serviceType);
        validatePrice(price);

        booking.setCarNumber(carNumber.trim());
        booking.setCarModel(carModel);
        booking.setServiceType(serviceType);
        booking.setScheduledAt(scheduledAt);
        booking.setPrice(price);

        bookingRepository.update(booking);
    }

    // смена статуса — только по разрешённому переходу
    public void changeStatus(int id, BookingStatus newStatus) {
        Booking booking = getById(id);
        BookingStatus current = booking.getStatus();

        if (!current.canChangeTo(newStatus)) {
            throw new BusinessException("Недопустимый переход статуса: "
                    + current.getTitle() + " -> " + newStatus.getTitle() + ".");
        }
        booking.setStatus(newStatus);
        bookingRepository.update(booking);
    }

    public void delete(int id) {
        Booking booking = getById(id);
        // запись, которая уже в работе, удалять нельзя
        if (booking.getStatus() == BookingStatus.IN_PROGRESS) {
            throw new BusinessException("Нельзя удалить запись, которая находится в работе.");
        }
        bookingRepository.deleteById(id);
    }

    // ---------- поиск ----------

    public List<Booking> searchByCarNumber(String carNumber) {
        return bookingRepository.searchByCarNumber(carNumber);
    }

    public List<Booking> searchByClientName(String name) {
        return bookingRepository.searchByClientName(name);
    }

    // ---------- фильтрация ----------

    public List<Booking> filterByStatus(BookingStatus status) {
        return bookingRepository.filterByStatus(status);
    }

    public List<Booking> filterByServiceType(ServiceType type) {
        return bookingRepository.filterByServiceType(type);
    }

    public List<Booking> filterByDateRange(LocalDateTime from, LocalDateTime to) {
        return bookingRepository.filterByDateRange(from, to);
    }

    // ---------- сортировка (Stream API) ----------

    public List<Booking> sortByDate(List<Booking> bookings) {
        return bookings.stream()
                .sorted(Comparator.comparing(Booking::getScheduledAt))
                .collect(Collectors.toList());
    }

    public List<Booking> sortByPriceDesc(List<Booking> bookings) {
        return bookings.stream()
                .sorted(Comparator.comparingDouble(Booking::getPrice).reversed())
                .collect(Collectors.toList());
    }

    public List<Booking> sortByStatus(List<Booking> bookings) {
        return bookings.stream()
                .sorted(Comparator.comparing((Booking b) -> b.getStatus().name()))
                .collect(Collectors.toList());
    }

    // ---------- статистика ----------

    public Statistics getStatistics() {
        List<Booking> all = bookingRepository.findAll();

        long completed = all.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).count();
        long cancelled = all.stream().filter(b -> b.getStatus() == BookingStatus.CANCELLED).count();
        long active = all.stream().filter(b -> !b.getStatus().isFinal()).count();
        double revenue = all.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED)
                .mapToDouble(Booking::getPrice)
                .sum();

        Statistics stats = new Statistics();
        stats.setTotalClients(clientRepository.findAll().size());
        stats.setTotalBookings(all.size());
        stats.setActiveBookings((int) active);
        stats.setCompletedBookings((int) completed);
        stats.setCancelledBookings((int) cancelled);
        stats.setTotalRevenue(revenue);
        return stats;
    }

    // ---------- проверки бизнес-правил ----------

    private void validateClientExists(int clientId) {
        if (!clientRepository.findById(clientId).isPresent()) {
            throw new BusinessException("Клиент с ID " + clientId + " не существует.");
        }
    }

    private void validateCarNumber(String carNumber) {
        if (carNumber == null || carNumber.trim().isEmpty()) {
            throw new BusinessException("Гос. номер автомобиля обязателен.");
        }
        String trimmed = carNumber.trim();
        if (trimmed.length() < 4 || trimmed.length() > 15) {
            throw new BusinessException("Гос. номер должен содержать от 4 до 15 символов.");
        }
        boolean hasLetter = trimmed.chars().anyMatch(Character::isLetter);
        boolean hasDigit = trimmed.chars().anyMatch(Character::isDigit);
        if (!trimmed.matches("[\\p{L}0-9 ]+") || !hasLetter || !hasDigit) {
            throw new BusinessException(
                    "Гос. номер должен состоять из букв и цифр (например: А123ВС77).");
        }
    }

    private void validateServiceType(ServiceType serviceType) {
        if (serviceType == null) {
            throw new BusinessException("Тип услуги обязателен.");
        }
    }

    private void validateFutureDate(LocalDateTime scheduledAt) {
        if (scheduledAt == null || scheduledAt.isBefore(LocalDateTime.now())) {
            throw new BusinessException("Дата записи должна быть в будущем.");
        }
    }

    private void validatePrice(double price) {
        if (Double.isNaN(price) || Double.isInfinite(price)) {
            throw new BusinessException("Цена указана некорректно.");
        }
        if (price < 0) {
            throw new BusinessException("Цена не может быть отрицательной.");
        }
        if (price > 1_000_000) {
            throw new BusinessException("Цена слишком большая (максимум 1 000 000 руб.).");
        }
    }
}
