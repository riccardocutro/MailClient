package it.unito.prog3.mailclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApp extends Application {
    @Override public void start(Stage stage) throws Exception {
        stage.setTitle("Mail Client - Login");
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/LoginView.fxml")), 360, 240));
        stage.show();
    }
    public static void main(String[] args) { launch(args); }
}
