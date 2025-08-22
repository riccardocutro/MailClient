package it.unito.prog3.mailclient.controller;

import it.unito.prog3.mailclient.net.ClientCore;
import it.unito.prog3.mailclient.util.EmailValidator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;

public class ComposeController {
    @FXML private TextField toField, subjectField;
    @FXML private TextArea bodyArea;

    private ClientCore core;
    private String from;

    public record Prefill(String to, String subject, String body) {}

    public void init(ClientCore core, String from, MainController.ComposePrefill pref) {
        this.core = core; this.from = from;
        if (pref != null) {
            if (pref.to()!=null) toField.setText(pref.to());
            if (pref.subject()!=null) subjectField.setText(pref.subject());
            if (pref.body()!=null) bodyArea.setText(pref.body());
        }
    }

    @FXML private void onCancel() {
        toField.getScene().getWindow().hide();
    }

    @FXML private void onSend() {
        try {
            List<String> to = EmailValidator.splitAndValidate(toField.getText());
            String subject = subjectField.getText() == null ? "" : subjectField.getText();
            String body = bodyArea.getText() == null ? "" : bodyArea.getText();
            core.send(from, to, subject, body);
            new Alert(Alert.AlertType.INFORMATION, "Messaggio inviato.").showAndWait();
            onCancel();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Errore invio: " + e.getMessage()).showAndWait();
        }
    }
}
