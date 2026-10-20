package carwash.fx;

import carwash.model.Booking;
import carwash.model.Client;
import carwash.model.ServiceType;
import carwash.service.BookingService;
import carwash.service.ClientService;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// контроллер формы добавления/редактирования записи.
// Получает данные от пользователя, передаёт их в сервис (там валидация и БД).
public class BookingDialogController {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private Label titleLabel;
    @FXML private ComboBox<Client> clientBox;
    @FXML private TextField carNumberField;
    @FXML private TextField carModelField;
    @FXML private ComboBox<ServiceType> serviceBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;
    @FXML private TextField priceField;
    @FXML private Label errorLabel;

    private Stage stage;
    private BookingService bookingService;
    private Booking editing;      // null — режим добавления
    private boolean saved;

    public void init(Stage stage, BookingService bookingService,
                     ClientService clientService, Booking booking) {
        this.stage = stage;
        this.bookingService = bookingService;
        this.editing = booking;

        setupClientBox(clientService);
        setupServiceBox();

        if (booking == null) {
            titleLabel.setText("Новая запись");
            timeField.setText("10:00");
            datePicker.setValue(LocalDate.now());
        } else {
            titleLabel.setText("Редактирование записи #" + booking.getId());
            fillForm(booking);
        }
    }

    public boolean isSaved() {
        return saved;
    }

    // связанная сущность: клиента выбираем из списка, а не вводим его id вручную
    private void setupClientBox(ClientService clientService) {
        clientBox.getItems().setAll(clientService.getAll());
        clientBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Client c) {
                return c == null ? "" : c.getFullName() + " (" + c.getPhone() + ")";
            }
            @Override
            public Client fromString(String s) {
                return null;
            }
        });
    }

    private void setupServiceBox() {
        serviceBox.getItems().setAll(ServiceType.values());
        serviceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(ServiceType t) {
                return t == null ? "" : t.getTitle();
            }
            @Override
            public ServiceType fromString(String s) {
                return null;
            }
        });
        // при выборе услуги подставляем её базовую цену, если поле ещё пустое
        serviceBox.valueProperty().addListener((obs, old, val) -> {
            if (val != null && priceField.getText().trim().isEmpty()) {
                priceField.setText(String.valueOf((long) val.getBasePrice()));
            }
        });
    }

    // заполняем форму значениями редактируемой записи
    private void fillForm(Booking b) {
        for (Client c : clientBox.getItems()) {
            if (c.getId() == b.getClientId()) {
                clientBox.setValue(c);
                break;
            }
        }
        // клиента у существующей записи не меняем
        clientBox.setDisable(true);

        carNumberField.setText(b.getCarNumber());
        carModelField.setText(b.getCarModel());
        serviceBox.setValue(b.getServiceType());
        datePicker.setValue(b.getScheduledAt().toLocalDate());
        timeField.setText(b.getScheduledAt().format(TIME_FORMAT));
        priceField.setText(String.format("%.2f", b.getPrice()));
    }

    @FXML
    private void onSave() {
        hideError();
        try {
            Client client = clientBox.getValue();
            if (client == null) {
                showError("Выберите клиента из списка.");
                return;
            }
            ServiceType service = serviceBox.getValue();
            if (service == null) {
                showError("Выберите тип услуги.");
                return;
            }
            LocalDate date = datePicker.getValue();
            if (date == null) {
                showError("Укажите дату записи.");
                return;
            }
            LocalTime time = parseTime(timeField.getText());
            if (time == null) {
                showError("Время должно быть в формате ЧЧ:ММ, например 14:30.");
                return;
            }
            Double price = parsePrice(priceField.getText());
            if (price == null) {
                showError("Цена должна быть числом, например 1200.");
                return;
            }

            LocalDateTime scheduledAt = LocalDateTime.of(date, time);
            String carNumber = carNumberField.getText();
            String carModel = emptyToNull(carModelField.getText());

            // валидацию выполняет сервис; если правило нарушено — ловим сообщение
            if (editing == null) {
                bookingService.create(client.getId(), carNumber, carModel, service, scheduledAt, price);
            } else {
                bookingService.update(editing.getId(), carNumber, carModel, service, scheduledAt, price);
            }
            saved = true;
            stage.close();
        } catch (RuntimeException e) {
            // понятное сообщение об ошибке вместо stack trace
            showError(e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        stage.close();
    }

    // ---------- вспомогательные методы ----------

    private LocalTime parseTime(String text) {
        try {
            return LocalTime.parse(text.trim(), TIME_FORMAT);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private Double parsePrice(String text) {
        try {
            return Double.parseDouble(text.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String emptyToNull(String text) {
        return text == null || text.trim().isEmpty() ? null : text.trim();
    }

    private void showError(String message) {
        errorLabel.setText(message == null ? "Неизвестная ошибка." : message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        stage.sizeToScene();
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
