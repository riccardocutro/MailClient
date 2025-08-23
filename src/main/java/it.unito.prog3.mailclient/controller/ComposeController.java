package it.unito.prog3.mailclient.controller;

import it.unito.prog3.mailclient.net.ClientCore;
import it.unito.prog3.mailclient.util.EmailValidator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * Controller della finestra di composizione messaggi (JavaFX).
 * Responsabilità:
 *
 *   Gestire i campi di input (To, Subject, Body) e le azioni utente (Invia/Annulla).
 *   Delegare l'invio al core di rete {@link ClientCore} e mostrare feedback all'utente.
 *
 * Ciclo di vita:
 *
 *   Il controller viene creato dal loader FXML.
 *   Il chiamante invoca {@link #init(ClientCore, String, MainController.ComposePrefill)} per
 *       fornire le dipendenze e prefilling opzionale.
 */
public class ComposeController {

    /** Campo destinatari (stringa con uno o più indirizzi, separati da virgola/;). */
    @FXML private TextField toField;
    /** Campo oggetto del messaggio. */
    @FXML private TextField subjectField;
    /** Area di testo per il corpo del messaggio. */
    @FXML private TextArea bodyArea;

    /** Facade verso il layer di rete del client (invio email). */
    private ClientCore core;
    /** Indirizzo mittente (account corrente). */
    private String from;

    /**
     * Struttura di supporto per precompilare i campi di composizione.
     * <p>È utile, ad esempio, quando si fa "Rispondi" o "Inoltra".</p>
     *
     * @param to      destinatari iniziali (può essere {@code null})
     * @param subject oggetto iniziale (può essere {@code null})
     * @param body    corpo iniziale (può essere {@code null})
     */
    public record Prefill(String to, String subject, String body) { }

    /**
     * Inizializza il controller con le dipendenze e, opzionalmente,
     * precompila i campi della UI.
     *
     * @param core  componente di rete responsabile dell'invio
     * @param from  indirizzo mittente (es. utente loggato)
     * @param pref  dati di precompilazione; se {@code null} non viene applicato nulla
     */
    public void init(ClientCore core, String from, MainController.ComposePrefill pref) {
        // Iniezione dipendenze (non usiamo @Inject per semplicità)
        this.core = core;
        this.from = from;

        // Prefill opzionale: settiamo solo i campi non null
        if (pref != null) {
            if (pref.to() != null)      toField.setText(pref.to());
            if (pref.subject() != null) subjectField.setText(pref.subject());
            if (pref.body() != null)    bodyArea.setText(pref.body());
        }
    }

    /**
     * Handler del pulsante "Annulla": chiude la finestra di composizione.
     * <p>Nota: la finestra è ottenuta dal nodo della scena; non distruggiamo il controller.</p>
     */
    @FXML
    private void onCancel() {
        // Chiude lo Stage associato a qualsiasi nodo della scena (qui usiamo toField)
        toField.getScene().getWindow().hide();
    }

    /**
     * Handler del pulsante "Invia".
     *
     *  Valida e normalizza i destinatari (lista di email).</li>
     *  Recupera oggetto e corpo, sostituendo con stringa vuota se null.</li>
     *  Invia tramite {@link ClientCore#send(String, List, String, String)}.</li>
     *  Mostra un alert di esito e chiude la finestra in caso di successo.</li>
     *
     * In caso di errore (validazione o I/O), mostra un alert di tipo ERROR con il messaggio.
     */
    @FXML
    private void onSend() {
        try {
            // Validazione/parse dei destinatari: accetta più indirizzi separati (virgola/;),
            //    normalizza e lancia eccezione se un indirizzo non è valido.
            List<String> to = EmailValidator.splitAndValidate(toField.getText());

            // 2) Tolleranza ai null: salviamo sempre stringhe non null verso il core
            String subject = subjectField.getText() == null ? "" : subjectField.getText();
            String body    = bodyArea.getText()      == null ? "" : bodyArea.getText();

            // 3) Invio sincrono. Se l'implementazione del core è bloccante e l'operazione
            //    può durare, valutare uno spostamento su thread di background (Task JavaFX).
            core.send(from, to, subject, body);

            // 4) Feedback all'utente + chiusura
            new Alert(Alert.AlertType.INFORMATION, "Messaggio inviato.").showAndWait();
            onCancel();
        } catch (Exception e) {
            // Catch ampio: include sia errori di validazione sia errori di rete.
            // Mostriamo il messaggio dell'eccezione per dare contesto all'utente.
            new Alert(Alert.AlertType.ERROR, "Errore invio: " + e.getMessage()).showAndWait();
        }
    }
}
