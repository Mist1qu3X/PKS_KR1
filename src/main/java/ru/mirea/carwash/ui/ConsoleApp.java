package ru.mirea.carwash.ui;

import ru.mirea.carwash.exception.BusinessException;
import ru.mirea.carwash.exception.DatabaseException;
import ru.mirea.carwash.exception.EntityNotFoundException;
import ru.mirea.carwash.model.Booking;
import ru.mirea.carwash.model.BookingStatus;
import ru.mirea.carwash.model.Client;
import ru.mirea.carwash.model.ServiceType;
import ru.mirea.carwash.repository.BookingRepository;
import ru.mirea.carwash.repository.ClientRepository;
import ru.mirea.carwash.service.BookingService;
import ru.mirea.carwash.service.ClientService;
import ru.mirea.carwash.service.Statistics;
import ru.mirea.carwash.util.CsvExporter;
import ru.mirea.carwash.util.DatabaseInitializer;
import ru.mirea.carwash.util.ExcelExporter;
import ru.mirea.carwash.util.Exporter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// консольное меню: выводит пункты, читает ввод и вызывает сервисы (SQL тут нет)
public class ConsoleApp {

    private static final DateTimeFormatter DISPLAY_DT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final ConsoleReader reader = new ConsoleReader();

    private final ClientService clientService;
    private final BookingService bookingService;

    public ConsoleApp() {
        // собираем слои: репозитории -> сервисы
        ClientRepository clientRepository = new ClientRepository();
        BookingRepository bookingRepository = new BookingRepository();
        this.clientService = new ClientService(clientRepository);
        this.bookingService = new BookingService(bookingRepository, clientRepository);
    }

    // главный цикл: работает, пока не выбрали "Выход"
    public void run() {
        System.out.println("Добро пожаловать в систему записи на автомойку!");
        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = reader.readInt("Выберите действие: ");
            try {
                switch (choice) {
                    case 1: clientsMenu(); break;
                    case 2: bookingsMenu(); break;
                    case 3: searchMenu(); break;
                    case 4: filterMenu(); break;
                    case 5: showStatistics(); break;
                    case 6: exportMenu(); break;
                    case 7: initDatabase(); break;
                    case 0: running = false; break;
                    default: System.out.println("Неизвестный пункт меню.");
                }
            } catch (BusinessException | EntityNotFoundException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (DatabaseException e) {
                System.out.println("Ошибка базы данных: " + e.getMessage());
            } catch (Exception e) {
                // страховка от непредвиденных ошибок, чтобы программа не падала
                System.out.println("Непредвиденная ошибка: " + e.getMessage());
            }
        }
        System.out.println("Работа завершена. До свидания!");
    }

    private void printMainMenu() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("       АВТОМОЙКА — СИСТЕМА ЗАПИСЕЙ");
        System.out.println("========================================");
        System.out.println("1. Клиенты");
        System.out.println("2. Записи на мойку");
        System.out.println("3. Поиск записей");
        System.out.println("4. Фильтрация записей");
        System.out.println("5. Статистика");
        System.out.println("6. Экспорт данных");
        System.out.println("7. Инициализировать / очистить базу данных");
        System.out.println("0. Выход");
    }

    // ==================== КЛИЕНТЫ ====================

    private void clientsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("--- Клиенты ---");
            System.out.println("1. Добавить клиента");
            System.out.println("2. Показать всех клиентов");
            System.out.println("3. Найти клиента по ID");
            System.out.println("4. Изменить клиента");
            System.out.println("5. Удалить клиента");
            System.out.println("0. Назад");
            int choice = reader.readInt("Выберите действие: ");
            try {
                switch (choice) {
                    case 1: addClient(); break;
                    case 2: showAllClients(); break;
                    case 3: findClientById(); break;
                    case 4: updateClient(); break;
                    case 5: deleteClient(); break;
                    case 0: back = true; break;
                    default: System.out.println("Неизвестный пункт меню.");
                }
            } catch (BusinessException | EntityNotFoundException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (DatabaseException e) {
                System.out.println("Ошибка базы данных: " + e.getMessage());
            }
        }
    }

    private void addClient() {
        String name = reader.readNonEmpty("Имя клиента: ");
        String phone = reader.readNonEmpty("Телефон (например +7-900-123-45-67): ");
        String email = reader.readLine("Email (можно оставить пустым): ");
        Client client = clientService.create(name, phone, email);
        System.out.println("Клиент добавлен: " + client);
    }

    private void showAllClients() {
        List<Client> clients = clientService.getAll();
        if (clients.isEmpty()) {
            System.out.println("Клиентов пока нет.");
            return;
        }
        System.out.println("Список клиентов:");
        for (Client c : clients) {
            System.out.println("  " + c);
        }
    }

    private void findClientById() {
        int id = reader.readInt("Введите ID клиента: ");
        System.out.println(clientService.getById(id));
    }

    private void updateClient() {
        int id = reader.readInt("ID клиента для изменения: ");
        Client current = clientService.getById(id);
        System.out.println("Текущие данные: " + current);
        System.out.println("(Оставьте поле пустым — значение не изменится.)");
        String name = reader.readLineOrKeep("Имя [" + current.getFullName() + "]: ", current.getFullName());
        String phone = reader.readLineOrKeep("Телефон [" + current.getPhone() + "]: ", current.getPhone());
        String email = reader.readLineOrKeep("Email [" + safe(current.getEmail()) + "]: ", current.getEmail());
        clientService.update(id, name, phone, email);
        System.out.println("Данные клиента обновлены.");
    }

    private void deleteClient() {
        int id = reader.readInt("ID клиента для удаления: ");
        Client client = clientService.getById(id);
        if (!reader.confirm("Удалить клиента \"" + client.getFullName() + "\"?")) {
            System.out.println("Удаление отменено.");
            return;
        }
        clientService.delete(id);
        System.out.println("Клиент удалён.");
    }

    // ==================== ЗАПИСИ ====================

    private void bookingsMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("--- Записи на мойку ---");
            System.out.println("1. Создать запись");
            System.out.println("2. Показать все записи");
            System.out.println("3. Найти запись по ID");
            System.out.println("4. Изменить запись");
            System.out.println("5. Изменить статус записи");
            System.out.println("6. Удалить запись");
            System.out.println("7. Сортировать записи");
            System.out.println("0. Назад");
            int choice = reader.readInt("Выберите действие: ");
            try {
                switch (choice) {
                    case 1: addBooking(); break;
                    case 2: showAllBookings(); break;
                    case 3: findBookingById(); break;
                    case 4: updateBooking(); break;
                    case 5: changeBookingStatus(); break;
                    case 6: deleteBooking(); break;
                    case 7: sortBookings(); break;
                    case 0: back = true; break;
                    default: System.out.println("Неизвестный пункт меню.");
                }
            } catch (BusinessException | EntityNotFoundException e) {
                System.out.println("Ошибка: " + e.getMessage());
            } catch (DatabaseException e) {
                System.out.println("Ошибка базы данных: " + e.getMessage());
            }
        }
    }

    private void addBooking() {
        // клиента выбираем из списка
        Client client = selectClient("Выберите клиента для записи:");
        if (client == null) {
            System.out.println("Создание записи отменено.");
            return;
        }
        String carNumber = reader.readNonEmpty("Гос. номер автомобиля (например А123ВС77): ");
        String carModel = reader.readLine("Модель автомобиля (можно оставить пустым): ");
        ServiceType serviceType = chooseServiceType();
        System.out.println("Когда записать на мойку?");
        LocalDateTime scheduledAt = reader.readFutureDateTime();
        double price = reader.readPriceWithDefault(
                "Цена, руб. (Enter — по умолчанию " + String.format("%.0f", serviceType.getBasePrice()) + "): ",
                serviceType.getBasePrice());

        // показываем итог и просим подтвердить
        System.out.println();
        System.out.println("Проверьте данные записи:");
        System.out.println("  Клиент: " + client.getFullName());
        System.out.println("  Авто:   " + carNumber + (carModel.isEmpty() ? "" : " (" + carModel + ")"));
        System.out.println("  Услуга: " + serviceType.getTitle());
        System.out.println("  Когда:  " + scheduledAt.format(DISPLAY_DT));
        System.out.printf("  Цена:   %.2f руб.%n", price);
        if (!reader.confirm("Создать запись?")) {
            System.out.println("Создание записи отменено.");
            return;
        }

        Booking booking = bookingService.create(client.getId(), carNumber, carModel,
                serviceType, scheduledAt, price);
        booking.setClientName(client.getFullName()); // чтобы сразу показать имя, а не id
        System.out.println("Запись создана: " + booking);
    }

    private void showAllBookings() {
        printBookings(bookingService.getAll());
    }

    private void findBookingById() {
        int id = reader.readInt("Введите ID записи: ");
        System.out.println(bookingService.getById(id));
    }

    private void updateBooking() {
        int id = reader.readInt("ID записи для изменения: ");
        Booking current = bookingService.getById(id);
        // завершённую/отменённую запись менять нельзя
        if (current.getStatus().isFinal()) {
            System.out.println("Нельзя изменить запись со статусом \""
                    + current.getStatus().getTitle() + "\".");
            return;
        }
        System.out.println("Текущие данные: " + current);
        System.out.println("(Оставьте поле пустым — значение не изменится.)");
        String carNumber = reader.readLineOrKeep(
                "Гос. номер [" + current.getCarNumber() + "]: ", current.getCarNumber());
        String carModel = reader.readLineOrKeep(
                "Модель [" + safe(current.getCarModel()) + "]: ", current.getCarModel());
        ServiceType serviceType = chooseServiceTypeOrKeep(current.getServiceType());
        LocalDateTime scheduledAt = reader.readDateTimeOrKeep(current.getScheduledAt());
        double price = reader.readPriceWithDefault(
                "Цена [" + String.format("%.2f", current.getPrice()) + "] (Enter — оставить): ",
                current.getPrice());

        bookingService.update(id, carNumber, carModel, serviceType, scheduledAt, price);
        System.out.println("Запись обновлена.");
    }

    private void changeBookingStatus() {
        int id = reader.readInt("ID записи: ");
        Booking booking = bookingService.getById(id);
        System.out.println("Текущий статус: " + booking.getStatus().getTitle());

        // показываем только разрешённые переходы
        List<BookingStatus> allowed = new ArrayList<>();
        for (BookingStatus status : BookingStatus.values()) {
            if (booking.getStatus().canChangeTo(status)) {
                allowed.add(status);
            }
        }
        if (allowed.isEmpty()) {
            System.out.println("У записи со статусом \"" + booking.getStatus().getTitle()
                    + "\" нет доступных переходов.");
            return;
        }
        System.out.println("Доступные статусы:");
        for (int i = 0; i < allowed.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + allowed.get(i).getTitle());
        }
        int choice = reader.readIntInRange("Номер статуса: ", 1, allowed.size());
        BookingStatus newStatus = allowed.get(choice - 1);
        bookingService.changeStatus(id, newStatus);
        System.out.println("Статус изменён на: " + newStatus.getTitle());
    }

    private void deleteBooking() {
        int id = reader.readInt("ID записи для удаления: ");
        Booking booking = bookingService.getById(id);
        System.out.println("Запись: " + booking);
        if (!reader.confirm("Удалить эту запись?")) {
            System.out.println("Удаление отменено.");
            return;
        }
        bookingService.delete(id);
        System.out.println("Запись удалена.");
    }

    private void sortBookings() {
        System.out.println("Сортировать:");
        System.out.println("1. По дате (по возрастанию)");
        System.out.println("2. По цене (по убыванию)");
        System.out.println("3. По статусу");
        int choice = reader.readInt("Выберите вариант: ");
        List<Booking> all = bookingService.getAll();
        if (choice == 1) {
            printBookings(bookingService.sortByDate(all));
        } else if (choice == 2) {
            printBookings(bookingService.sortByPriceDesc(all));
        } else if (choice == 3) {
            printBookings(bookingService.sortByStatus(all));
        } else {
            System.out.println("Неверный выбор.");
        }
    }

    // ==================== ПОИСК ====================

    private void searchMenu() {
        System.out.println();
        System.out.println("--- Поиск записей ---");
        System.out.println("1. По гос. номеру автомобиля");
        System.out.println("2. По имени клиента");
        System.out.println("0. Назад");
        int choice = reader.readInt("Выберите действие: ");
        switch (choice) {
            case 1:
                String number = reader.readNonEmpty("Введите гос. номер (или его часть): ");
                printBookings(bookingService.searchByCarNumber(number));
                break;
            case 2:
                String name = reader.readNonEmpty("Введите имя клиента (или его часть): ");
                printBookings(bookingService.searchByClientName(name));
                break;
            case 0:
                break;
            default:
                System.out.println("Неизвестный пункт меню.");
        }
    }

    // ==================== ФИЛЬТРАЦИЯ ====================

    private void filterMenu() {
        System.out.println();
        System.out.println("--- Фильтрация записей ---");
        System.out.println("1. По статусу");
        System.out.println("2. По типу услуги");
        System.out.println("3. По диапазону дат");
        System.out.println("0. Назад");
        int choice = reader.readInt("Выберите действие: ");
        switch (choice) {
            case 1:
                BookingStatus status = chooseStatus();
                printBookings(bookingService.filterByStatus(status));
                break;
            case 2:
                ServiceType type = chooseServiceType();
                printBookings(bookingService.filterByServiceType(type));
                break;
            case 3:
                LocalDate fromDate = reader.readDate("Дата начала (ДД.ММ.ГГГГ): ");
                LocalDate toDate = reader.readDate("Дата конца (ДД.ММ.ГГГГ): ");
                LocalDateTime from = fromDate.atStartOfDay();
                LocalDateTime to = toDate.atTime(23, 59, 59);
                printBookings(bookingService.filterByDateRange(from, to));
                break;
            case 0:
                break;
            default:
                System.out.println("Неизвестный пункт меню.");
        }
    }

    // ==================== СТАТИСТИКА ====================

    private void showStatistics() {
        Statistics stats = bookingService.getStatistics();
        System.out.println();
        System.out.println("--- Статистика системы ---");
        System.out.println("Всего клиентов: " + stats.getTotalClients());
        System.out.println("Всего записей: " + stats.getTotalBookings());
        System.out.println("Активных записей: " + stats.getActiveBookings());
        System.out.println("Завершённых записей: " + stats.getCompletedBookings());
        System.out.println("Отменённых записей: " + stats.getCancelledBookings());
        System.out.printf("Выручка по завершённым записям: %.2f руб.%n", stats.getTotalRevenue());
    }

    // ==================== ЭКСПОРТ ====================

    private void exportMenu() {
        System.out.println();
        System.out.println("--- Экспорт данных ---");
        System.out.println("1. Excel (.xlsx)");
        System.out.println("2. CSV (.csv)");
        System.out.println("0. Назад");
        int choice = reader.readInt("Выберите формат: ");
        Exporter exporter;
        switch (choice) {
            case 1: exporter = new ExcelExporter(); break;
            case 2: exporter = new CsvExporter(); break;
            case 0: return;
            default:
                System.out.println("Неизвестный формат.");
                return;
        }
        // Excel или CSV — конкретная реализация Exporter
        List<Booking> bookings = bookingService.getAll();
        if (bookings.isEmpty()) {
            System.out.println("Нет данных для экспорта.");
            return;
        }
        String fileName = exporter.getDefaultFileName();
        exporter.export(bookings, fileName);
        System.out.println("Данные экспортированы в файл: " + fileName);
    }

    // ==================== БАЗА ДАННЫХ ====================

    private void initDatabase() {
        System.out.println("Внимание: таблицы будут пересозданы, все текущие данные удалены.");
        if (reader.confirm("Продолжить?")) {
            DatabaseInitializer.initialize();
        } else {
            System.out.println("Отменено.");
        }
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    // выбор клиента из списка; null — отмена или клиентов нет
    private Client selectClient(String title) {
        List<Client> clients = clientService.getAll();
        if (clients.isEmpty()) {
            System.out.println("В системе нет клиентов. Сначала добавьте клиента (меню «Клиенты»).");
            return null;
        }
        System.out.println(title);
        for (int i = 0; i < clients.size(); i++) {
            Client c = clients.get(i);
            System.out.println("  " + (i + 1) + ". " + c.getFullName() + " (тел: " + c.getPhone() + ")");
        }
        System.out.println("  0. Отмена");
        int choice = reader.readIntInRange("Выберите клиента: ", 0, clients.size());
        if (choice == 0) {
            return null;
        }
        return clients.get(choice - 1);
    }

    private ServiceType chooseServiceType() {
        System.out.println("Выберите тип услуги:");
        ServiceType[] types = ServiceType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.println("  " + (i + 1) + ". " + types[i].getTitle()
                    + " (" + String.format("%.0f", types[i].getBasePrice()) + " руб.)");
        }
        int choice = reader.readIntInRange("Номер услуги: ", 1, types.length);
        return types[choice - 1];
    }

    private ServiceType chooseServiceTypeOrKeep(ServiceType current) {
        System.out.println("Тип услуги (0 — оставить текущий: " + current.getTitle() + "):");
        ServiceType[] types = ServiceType.values();
        for (int i = 0; i < types.length; i++) {
            System.out.println("  " + (i + 1) + ". " + types[i].getTitle()
                    + " (" + String.format("%.0f", types[i].getBasePrice()) + " руб.)");
        }
        int choice = reader.readIntInRange("Номер услуги: ", 0, types.length);
        return choice == 0 ? current : types[choice - 1];
    }

    private BookingStatus chooseStatus() {
        System.out.println("Выберите статус:");
        BookingStatus[] all = BookingStatus.values();
        for (int i = 0; i < all.length; i++) {
            System.out.println("  " + (i + 1) + ". " + all[i].getTitle());
        }
        int choice = reader.readIntInRange("Номер статуса: ", 1, all.length);
        return all[choice - 1];
    }

    private void printBookings(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            System.out.println("Записи не найдены.");
            return;
        }
        System.out.println("Найдено записей: " + bookings.size());
        for (Booking b : bookings) {
            System.out.println("  " + b);
        }
    }

    // null -> пустая строка (для аккуратного вывода)
    private String safe(String value) {
        return value == null ? "" : value;
    }
}
