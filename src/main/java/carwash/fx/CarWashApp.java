package carwash.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// точка входа JavaFX-приложения (КР2).
// Загружает главное окно из FXML и подключает CSS-оформление.
public class CarWashApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/carwash/fx/main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1000, 620);
        scene.getStylesheets().add(
                getClass().getResource("/carwash/fx/styles.css").toExternalForm());

        stage.setTitle("Автомойка — записи клиентов (КР2)");
        stage.setScene(scene);
        stage.setMinWidth(820);
        stage.setMinHeight(520);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
