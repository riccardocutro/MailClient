package it.unito.prog3.mailclient.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField emailField;

    @FXML
    private void handleLogin() {
        System.out.println("Login con: " + emailField.getText());
    }
}

