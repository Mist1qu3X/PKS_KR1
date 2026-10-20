package carwash.fx;

import carwash.model.Booking;
import carwash.model.BookingStatus;
import carwash.model.ServiceType;
import carwash.repository.BookingRepository;
import carwash.repository.ClientRepository;
import carwash.service.BookingService;
import carwash.service.ClientService;
import carwash.service.Statistics;
import carwash.util.CsvExporter;
import carwash.util.ExcelExporter;
import carwash.util.Exporter;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// главный контроллер: получает действия пользователя и координирует работу
// интерфейса. Вся бизнес-логика и работа с БД — в Service и Repository.
public class MainController {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML private TableView<Booking> table;
    @FXML private TableColumn<Booking, Integer> idCol;
    @FXML private TableColumn<Booking, String> clientCol;
    @FXML private TableColumn<Booking, String> carNumberCol;
    @FXML private TableColumn<Booking, String> carModelCol;
    @FXML private TableColumn<Booking, String> serviceCol;
    @FXML private TableColumn<Booking, LocalDateTime> dateCol;
    @FXML private TableColumn<Booking, Double> priceCol;
    @FXML private TableColumn<Booking, String> statusCol;

    @FXML private TextField searchField;
    @FXML private ComboBox<BookingStatus> statusFilter;
    @FXML private ComboBox<ServiceType> serviceFilter;
    @FXML private ComboBox<String> sortBox;
    @FXML private Label statusBar;

    // слои доступа к данным и бизнес-логики
    private final BookingRepository bookingRepository = new BookingRepository();
    private final ClientRepository clientRepository = new ClientRepository();
    private final BookingService bookingService =
            new BookingService(bookingRepository, clientRepository);
    private final ClientService clientService = new ClientService(clientRepository);

    // все загруженные записи; отображаемые получаем через фильтр и сортировку
    private final ObservableList<Booking> masterData = FXCollections.observableArrayList();
    private FilteredList<Booking> filteredData;
    private SortedList<Booking> sortedData;

    @FXML
    private void initialize() {
        setupColumns();
        setupFilters();
        setupSortBox();

        // фильтруем по строке поиска/фильтрам, затем сортируем
        filteredData = new FilteredList<>(masterData, b -> true);
        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // при изменении поиска или фильтров — пересобираем условие отбора
        searchField.textProperty().addListener((obs, o, n) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, o, n) -> applyFilters());
        serviceFilter.valueProperty().addListener((obs, o, n) -> applyFilters());

        loadData();
    }

    // --- настройка таблицы (какие поля показываем в столбцах) ---
    private void setupColumns() {
        idCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
        clientCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getClientName()));
        carNumberCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCarNumber()));
        carModelCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCarModel()));
        serviceCol.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getServiceType().getTitle()));
        statusCol.setCellValueFactory(
                c -> new SimpleStringProperty(c.getValue().getStatus().getTitle()));

        // дата хранится как LocalDateTime (чтобы сортировалась по времени),
        // а пользователю показываем её в удобном формате
        dateCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getScheduledAt()));
        dateCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(DATE_FORMAT));
            }
        });

        priceCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrice()));
        priceCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item));
            }
        });
    }

    // --- фильтры по статусу и типу услуги (null = «все») ---
    private void setupFilters() {
        statusFilter.getItems().add(null);
        statusFilter.getItems().addAll(BookingStatus.values());
        statusFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(BookingStatus s) {
                return s == null ? "Все статусы" : s.getTitle();
            }
            @Override
            public BookingStatus fromString(String s) {
                return null;
            }
        });
        statusFilter.getSelectionModel().selectFirst();

        serviceFilter.getItems().add(null);
        serviceFilter.getItems().addAll(ServiceType.values());
        serviceFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(ServiceType t) {
                return t == null ? "Все услуги" : t.getTitle();
            }
            @Override
            public ServiceType fromString(String s) {
                return null;
            }
        });
        serviceFilter.getSelectionModel().selectFirst();
    }

    // --- варианты сортировки (минимум по двум параметрам) ---
    private void setupSortBox() {
        sortBox.getItems().addAll(
                "Дата (сначала ранние)",
                "Дата (сначала поздние)",
                "Цена (по возрастанию)",
                "Цена (по убыванию)",
                "Статус");
        sortBox.valueProperty().addListener((obs, o, n) -> applySort(n));
    }

    // собираем условие отбора из строки поиска и двух фильтров
    private void applyFilters() {
        String query = searchField.getText() == null
                ? "" : searchField.getText().trim().toLowerCase();
        BookingStatus status = statusFilter.getValue();
        ServiceType service = serviceFilter.getValue();

        filteredData.setPredicate(b -> {
            // поиск по гос. номеру и имени клиента
            boolean matchesSearch = query.isEmpty()
                    || (b.getCarNumber() != null && b.getCarNumber().toLowerCase().contains(query))
                    || (b.getClientName() != null && b.getClientName().toLowerCase().contains(query));
            boolean matchesStatus = status == null || b.getStatus() == status;
            boolean matchesService = service == null || b.getServiceType() == service;
            return matchesSearch && matchesStatus && matchesService;
        });
        updateStatusBar();
    }

    // сортировка средствами TableView (через её столбцы)
    private void applySort(String choice) {
        if (choice == null) {
            return;
        }
        table.getSortOrder().clear();
        switch (choice) {
            case "Дата (сначала ранние)" -> sortByColumn(dateCol, SortType.ASCENDING);
            case "Дата (сначала поздние)" -> sortByColumn(dateCol, SortType.DESCENDING);
            case "Цена (по возрастанию)" -> sortByColumn(priceCol, SortType.ASCENDING);
            case "Цена (по убыванию)" -> sortByColumn(priceCol, SortType.DESCENDING);
            case "Статус" -> sortByColumn(statusCol, SortType.ASCENDING);
            default -> { }
        }
    }

    private void sortByColumn(TableColumn<Booking, ?> column, SortType type) {
        column.setSortType(type);
        table.getSortOrder().setAll(column);
        table.sort();
    }

    // загрузка данных из базы (через сервис)
    private void loadData() {
        try {
            masterData.setAll(bookingService.getAll());
            updateStatusBar();
        } catch (RuntimeException e) {
            statusBar.setText("Нет подключения к базе данных");
            Dialogs.error(e.getMessage());
        }
    }

    private void updateStatusBar() {
        statusBar.setText("Показано записей: " + sortedData.size()
                + " из " + masterData.size());
    }

    // ---------- обработчики кнопок ----------

    @FXML
    private void onAdd() {
        openBookingDialog(null);
    }

    @FXML
    private void onEdit() {
        Booking selected = requireSelection();
        if (selected != null) {
            openBookingDialog(selected);
        }
    }

    @FXML
    private void onChangeStatus() {
        Booking selected = requireSelection();
        if (selected == null) {
            return;
        }

        // показываем только статусы, в которые можно перейти из текущего
        List<String> allowed = new ArrayList<>();
        for (BookingStatus s : BookingStatus.values()) {
            if (selected.getStatus().canChangeTo(s)) {
                allowed.add(s.getTitle());
            }
        }
        if (allowed.isEmpty()) {
            Dialogs.info("Смена статуса",
                    "Для записи со статусом \"" + selected.getStatus().getTitle()
                            + "\" смена статуса недоступна.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(allowed.get(0), allowed);
        dialog.setTitle("Смена статуса");
        dialog.setHeaderText("Текущий статус: " + selected.getStatus().getTitle());
        dialog.setContentText("Новый статус:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        BookingStatus newStatus = statusByTitle(result.get());
        try {
            bookingService.changeStatus(selected.getId(), newStatus);
            loadData();
        } catch (RuntimeException e) {
            Dialogs.error(e.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        Booking selected = requireSelection();
        if (selected == null) {
            return;
        }
        boolean ok = Dialogs.confirm("Удаление записи",
                "Удалить запись #" + selected.getId() + " ("
                        + selected.getCarNumber() + ")?");
        if (!ok) {
            return;
        }
        try {
            bookingService.delete(selected.getId());
            loadData();
        } catch (RuntimeException e) {
            Dialogs.error(e.getMessage());
        }
    }

    @FXML
    private void onRefresh() {
        loadData();
    }

    @FXML
    private void onResetFilters() {
        searchField.clear();
        statusFilter.getSelectionModel().selectFirst();
        serviceFilter.getSelectionModel().selectFirst();
        sortBox.getSelectionModel().clearSelection();
        table.getSortOrder().clear();
        applyFilters();
    }

    @FXML
    private void onStatistics() {
        try {
            Statistics stats = bookingService.getStatistics();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/carwash/fx/stats-dialog.fxml"));
            Parent root = loader.load();
            StatsController controller = loader.getController();

            Stage dialog = newDialog(root, "Статистика");
            controller.init(dialog, stats);
            dialog.showAndWait();
        } catch (Exception e) {
            Dialogs.error("Не удалось показать статистику: " + e.getMessage());
        }
    }

    @FXML
    private void onExport() {
        if (sortedData.isEmpty()) {
            Dialogs.info("Экспорт", "Нет данных для экспорта.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить данные");
        chooser.setInitialFileName("bookings.xlsx");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Excel (*.xlsx)", "*.xlsx"),
                new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));

        File file = chooser.showSaveDialog(window());
        if (file == null) {
            return;
        }
        try {
            List<Booking> data = new ArrayList<>(sortedData);
            Exporter exporter = file.getName().toLowerCase().endsWith(".csv")
                    ? new CsvExporter() : new ExcelExporter();
            exporter.export(data, file.getAbsolutePath());
            Dialogs.info("Экспорт", "Данные сохранены в файл:\n" + file.getAbsolutePath());
        } catch (RuntimeException e) {
            Dialogs.error(e.getMessage());
        }
    }

    // ---------- вспомогательные методы ----------

    private void openBookingDialog(Booking booking) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/carwash/fx/booking-dialog.fxml"));
            Parent root = loader.load();
            BookingDialogController controller = loader.getController();

            Stage dialog = newDialog(root, booking == null ? "Новая запись" : "Редактирование записи");
            controller.init(dialog, bookingService, clientService, booking);
            dialog.showAndWait();

            if (controller.isSaved()) {
                loadData();
            }
        } catch (Exception e) {
            Dialogs.error("Не удалось открыть форму: " + e.getMessage());
        }
    }

    // создаёт модальное окно поверх главного, подключает тот же CSS
    private Stage newDialog(Parent root, String title) {
        Stage dialog = new Stage();
        dialog.initOwner(window());
        dialog.initModality(Modality.APPLICATION_MODAL);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                getClass().getResource("/carwash/fx/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.setTitle(title);
        dialog.setResizable(false);
        return dialog;
    }

    private Window window() {
        return table.getScene().getWindow();
    }

    // проверяем, что строка выбрана, иначе подсказываем пользователю
    private Booking requireSelection() {
        Booking selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Dialogs.info("Нет выбора", "Сначала выберите запись в таблице.");
        }
        return selected;
    }

    private BookingStatus statusByTitle(String title) {
        for (BookingStatus s : BookingStatus.values()) {
            if (s.getTitle().equals(title)) {
                return s;
            }
        }
        return null;
    }
}
