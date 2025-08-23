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
    @FXML private ListView<InboxItem> inboxList; // lista della posta in arrivo
    @FXML private Label statusLabel, subjectLabel, fromLabel, toLabel, dateLabel; // etichette dettaglio
    @FXML private TextArea bodyArea; // corpo del messaggio selezionato

    private ClientCore core;         // componente di rete
    private ClientState state;       // stato condiviso (inbox, utente)
    private PollingService polling;  // servizio di polling per aggiornare la inbox periodicamente

    // Inizializzazione del controller: setta core, stato e avvia il polling
    public void init(ClientCore core, ClientState state) {
        this.core = core;
        this.state = state;

        // collega la lista agli elementi di stato (ObservableList)
        inboxList.setItems(state.inbox());

        // personalizza la visualizzazione di ogni riga della inbox
        inboxList.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(InboxItem it, boolean empty) {
                super.updateItem(it, empty);
                setText(empty || it == null ? null :
                        "#" + it.getId() + "  " + it.getSubject() + "  —  " + it.getFrom());
            }
        });

        // quando cambia selezione nella lista, mostra i dettagli
        inboxList.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> showDetail(b));

        // stato iniziale: non connesso (finché polling non riesce)
        statusLabel.setText("Non connesso");

        // avvia polling per aggiornare inbox e stato connessione
        polling = new PollingService(core, state);
        polling.setOnFailed(e -> statusLabel.setText("Non connesso")); // se fallisce
        polling.setOnSucceeded(e -> statusLabel.setText("Connesso"));  // se ok
        polling.start();
    }

    // Mostra il dettaglio di un messaggio selezionato
    private void showDetail(InboxItem it) {
        if (it == null) {
            // se nessuna selezione, svuota i campi
            subjectLabel.setText("");
            fromLabel.setText("");
            toLabel.setText("");
            dateLabel.setText("");
            bodyArea.setText("");
            return;
        }
        // riempi le etichette con i dati del messaggio
        subjectLabel.setText(it.getSubject());
        fromLabel.setText("Da: " + it.getFrom());
        dateLabel.setText("Data: " + it.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        // campo "To" non mostrato (vuoto), ma puoi implementare se serve
        toLabel.setText("");
        bodyArea.setText(it.getBody());
    }

    // Apre finestra di composizione per nuovo messaggio
    @FXML private void onNew() { openCompose(null); }

    // Rispondi solo al mittente
    @FXML private void onReply() {
        var it = inboxList.getSelectionModel().getSelectedItem();
        if (it == null) return;
        openCompose(new ComposePrefill(
                it.getFrom(),
                "Re: " + it.getSubject(),
                "\n\n--- Risposta ---\n" + it.getPreview()
        ));
    }

    // Rispondi a tutti tranne te stesso
    @FXML private void onReplyAll() {
        var it = inboxList.getSelectionModel().getSelectedItem();
        if (it == null) return;

        var my = state.userEmailProperty().get();

        // set per evitare duplicati, ordine preservato
        java.util.LinkedHashSet<String> recipients = new java.util.LinkedHashSet<>();
        recipients.add(it.getFrom());
        recipients.addAll(it.getTo());
        recipients.remove(my); // rimuovi il tuo indirizzo

        if (recipients.isEmpty()) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Nessun destinatario valido per 'Rispondi a tutti'.").showAndWait();
            return;
        }

        String toCsv = String.join(",", recipients);
        // se già inizia con "Re:" non aggiungere un altro
        String subj = it.getSubject().startsWith("Re:") ? it.getSubject() : "Re: " + it.getSubject();
        String body = "\n\n--- Risposta a tutti ---\n" + it.getBody();

        openCompose(new ComposePrefill(toCsv, subj, body));
    }

    // Inoltra messaggio: destinatario vuoto, oggetto con "Fwd:"
    @FXML private void onForward() {
        var it = inboxList.getSelectionModel().getSelectedItem();
        if (it == null) return;
        openCompose(new ComposePrefill(
                "",
                "Fwd: " + it.getSubject(),
                "\n\n--- Inoltrato ---\n" + it.getPreview()
        ));
    }

    // Elimina il messaggio selezionato
    @FXML private void onDelete() {
        var it = inboxList.getSelectionModel().getSelectedItem();
        if (it == null) return;
        try {
            // chiede al server di eliminare, se ok rimuove dall'elenco locale
            if (core.delete(state.userEmailProperty().get(), it.getId())) {
                state.inbox().remove(it);
            } else {
                new Alert(Alert.AlertType.ERROR, "Eliminazione fallita.").showAndWait();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Errore: " + e.getMessage()).showAndWait();
        }
    }

    // record per precompilare la finestra di composizione
    public record ComposePrefill(String to, String subject, String body) {}

    // Apre la finestra di composizione (nuovo, rispondi, inoltra)
    private void openCompose(ComposePrefill pref) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource("/ComposeDialog.fxml"));
            DialogPane pane = l.load();
            ComposeController c = l.getController();
            c.init(core, state.userEmailProperty().get(), pref);

            Dialog<Void> d = new Dialog<>();
            d.setDialogPane(pane);
            d.setTitle("Nuovo messaggio");
            d.showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR,
                    "Errore apertura compose: " + e.getMessage()).showAndWait();
        }
    }

    // Arresta eventuali thread o servizi in background quando si chiude l'app
    public void shutdown() {
        if (polling != null) polling.cancel();
    }
}
