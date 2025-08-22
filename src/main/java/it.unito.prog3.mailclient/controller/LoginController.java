package it.unito.prog3.mailclient.controller;

import it.unito.prog3.mailclient.model.ClientState;
import it.unito.prog3.mailclient.net.ClientCore;
import it.unito.prog3.mailclient.service.PollingService;
import it.unito.prog3.mailclient.util.EmailValidator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        hostField.setText("localhost");
        portField.setText("5555");
    }

    @FXML
    private void onLogin() {
        errorLabel.setText("");
        String email = emailField.getText() == null ? "" : emailField.getText().trim().toLowerCase();
        if (!EmailValidator.isValid(email)) {
            errorLabel.setText("Email non valida.");
            return;
        }
        int port;
        try { port = Integer.parseInt(portField.getText().trim()); }
        catch (Exception e) { errorLabel.setText("Porta non valida."); return; }

        String host = hostField.getText().trim();
        ClientCore core = new ClientCore(host, port);
        try {
            if (!core.login(email)) { errorLabel.setText("Utente non esiste sul server."); return; }
        } catch (Exception ex) {
            errorLabel.setText("Connessione fallita: " + ex.getMessage());
            return;
        }

        // prepara stato + polling
        ClientState state = new ClientState();
        state.userEmailProperty().set(email);

        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Scene scene = new Scene(l.load(), 900, 600);
            MainController controller = l.getController();
            controller.init(core, state);

            Stage stage = new Stage();
            stage.setTitle("Mail Client - " + email);
            stage.setScene(scene);
            stage.show();

            // chiudi login
            ((Stage) emailField.getScene().getWindow()).close();
        } catch (Exception e) {
            errorLabel.setText("Errore apertura finestra principale: " + e.getMessage());
        }
    }
}
