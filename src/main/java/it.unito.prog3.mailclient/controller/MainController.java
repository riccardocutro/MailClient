package it.unito.prog3.mailclient.controller;

import it.unito.prog3.mailclient.model.ClientState;
import it.unito.prog3.mailclient.model.InboxItem;
import it.unito.prog3.mailclient.net.ClientCore;
import it.unito.prog3.mailclient.service.PollingService;
import it.unito.prog3.mailclient.util.EmailValidator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainController {
    @FXML private ListView<InboxItem> inboxList;
    @FXML private Label statusLabel, subjectLabel, fromLabel, toLabel, dateLabel;
    @FXML private TextArea bodyArea;

    private ClientCore core;
    private ClientState state;
    private PollingService polling;

    public void init(ClientCore core, ClientState state) {
        this.core = core;
        this.state = state;

        inboxList.setItems(state.inbox());
        inboxList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(InboxItem it, boolean empty) {
                super.updateItem(it, empty);
                setText(empty || it==null ? null :
                        "#" + it.getId() + "  " + it.getSubject() + "  —  " + it.getFrom());
            }
        });

        inboxList.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> showDetail(b));
        statusLabel.setText("Non connesso");

        // start polling
        polling = new PollingService(core, state);
        polling.setOnFailed(e -> statusLabel.setText("Non connesso"));
        polling.setOnSucceeded(e -> statusLabel.setText("Connesso"));
        polling.start();
    }

    private void showDetail(InboxItem it) {
        if (it == null) {
            subjectLabel.setText(""); fromLabel.setText(""); toLabel.setText("");
            dateLabel.setText(""); bodyArea.setText(""); return;
        }
        subjectLabel.setText(it.getSubject());
        fromLabel.setText("Da: " + it.getFrom());
        dateLabel.setText("Data: " + it.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        toLabel.setText("");
        bodyArea.setText(it.getBody());
    }

    @FXML private void onNew() { openCompose(null); }
    @FXML private void onReply() {
        var it = inboxList.getSelectionModel().getSelectedItem();
        if (it==null) return;
        openCompose(new ComposePrefill(it.getFrom(), "Re: " + it.getSubject(), "\n\n--- Risposta ---\n" + it.getPreview()));
    }
    @FXML private void onReplyAll() {  var it = inboxList.getSelectionModel().getSelectedItem();
        if (it == null) return;

        var my = state.userEmailProperty().get();

        java.util.LinkedHashSet<String> recipients = new java.util.LinkedHashSet<>();
        recipients.add(it.getFrom());
        recipients.addAll(it.getTo());
        recipients.remove(my);

        if (recipients.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION, "Nessun destinatario valido per 'Rispondi a tutti'.").showAndWait();
            return;
        }

        String toCsv = String.join(",", recipients);
        String subj  = it.getSubject().startsWith("Re:") ? it.getSubject() : "Re: " + it.getSubject();
        String body  = "\n\n--- Risposta a tutti ---\n" + it.getBody();

        openCompose(new ComposePrefill(toCsv, subj, body));
    }

    @FXML private void onForward() {
        var it = inboxList.getSelectionModel().getSelectedItem();
        if (it==null) return;
        openCompose(new ComposePrefill("", "Fwd: " + it.getSubject(), "\n\n--- Inoltrato ---\n" + it.getPreview()));
    }

    @FXML private void onDelete() {
        var it = inboxList.getSelectionModel().getSelectedItem();
        if (it==null) return;
        try {
            if (core.delete(state.userEmailProperty().get(), it.getId())) {
                state.inbox().remove(it);
            } else {
                new Alert(Alert.AlertType.ERROR, "Eliminazione fallita.").showAndWait();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Errore: " + e.getMessage()).showAndWait();
        }
    }

    public record ComposePrefill(String to, String subject, String body) {}
    private void openCompose(ComposePrefill pref) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource("/ComposeDialog.fxml"));
            DialogPane pane = l.load();
            ComposeController c = l.getController();
            c.init(core, state.userEmailProperty().get(), pref);
            Dialog<Void> d = new Dialog<>();
            d.setDialogPane(pane); d.setTitle("Nuovo messaggio");
            d.showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Errore apertura compose: " + e.getMessage()).showAndWait();
        }
    }

    public void shutdown() {
        if (polling != null) polling.cancel();
    }

}
