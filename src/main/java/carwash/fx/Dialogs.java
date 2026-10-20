package carwash.fx;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

// небольшой помощник для стандартных диалоговых окон JavaFX,
// чтобы не повторять один и тот же код в разных контроллерах.
public final class Dialogs {

    private Dialogs() {
    }

    public static void info(String title, String message) {
        show(Alert.AlertType.INFORMATION, title, message);
    }

    public static void error(String message) {
        show(Alert.AlertType.ERROR, "Ошибка", message);
    }

    // окно подтверждения (да/нет). true — если пользователь нажал OK.
    public static boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message,
                ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle(title);
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
