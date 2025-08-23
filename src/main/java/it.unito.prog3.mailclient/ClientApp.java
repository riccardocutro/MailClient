package it.unito.prog3.mailclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Titolo della finestra iniziale
        stage.setTitle("Mail Client - Login");
        // Carica la scena di login dal file FXML e la imposta sulla finestra
        stage.setScene(new Scene(
                FXMLLoader.load(getClass().getResource("/LoginView.fxml")),
                360, 240
        ));
        // Mostra la finestra
        stage.show();
    }

    // entry point dell'applicazione JavaFX
    public static void main(String[] args) {
        launch(args);
    }
}
