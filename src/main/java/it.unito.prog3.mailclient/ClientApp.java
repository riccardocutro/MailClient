package it.unito.prog3.mailclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginView.fxml"));
        stage.setScene(new Scene(loader.load(), 400, 200));
        stage.setTitle("Mail Client - Login");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
