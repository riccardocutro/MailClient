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

/**
 * Controller della schermata di login (JavaFX).
 * <p>
 * Responsabilità:
 * <ul>
 *   <li>Raccogliere e validare le credenziali di accesso (email) e i parametri di connessione (host/port).</li>
 *   <li>Stabilire la connessione al server tramite {@link ClientCore} e verificare l'esistenza utente.</li>
 *   <li>In caso di successo, inizializzare e mostrare la finestra principale {@link MainController}.</li>
 * </ul>
 * UX: gli errori sono mostrati in {@link #errorLabel}.
 */
public class LoginController {

    /** Campo email utente (mittente). */
    @FXML private TextField emailField;
    /** Campo host del server (es. "localhost"). */
    @FXML private TextField hostField;
    /** Campo porta del server (es. 5555). */
    @FXML private TextField portField;
    /** Etichetta per messaggi d'errore validazione/connessione. */
    @FXML private Label errorLabel;

    /**
     * Hook di inizializzazione chiamato da JavaFX dopo il caricamento FXML.
     * Imposta valori predefiniti per host/porta, utili in sviluppo locale.
     */
    @FXML
    public void initialize() {
        hostField.setText("localhost");
        portField.setText("5555");
    }

    /**
     * Handler del pulsante "Login".
     * Pulisce eventuali messaggi d'errore precedenti.
     *   Normalizza e valida l'email.
     *   Valida la porta.
     *   Tenta la connessione e il login lato server tramite {@link ClientCore}.
     *   In caso di esito positivo, apre la finestra principale e chiude quella di login.
     * Errori/exception vengono comunicati all'utente tramite {@link #errorLabel}.
     */
    @FXML
    private void onLogin() {
        // Reset messaggio d'errore
        errorLabel.setText("");

        // Normalizziamo l'email: trim + lowercase per coerenza lato server
        String email = emailField.getText() == null ? "" : emailField.getText().trim().toLowerCase();

        // Validazione formale dell'email
        if (!EmailValidator.isValid(email)) {
            errorLabel.setText("Email non valida.");
            return;
        }

        // Parsing sicuro della porta (intero positivo). In caso di input non numerico, segnaliamo errore.
        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (Exception e) {
            errorLabel.setText("Porta non valida.");
            return;
        }

        // Host può essere nome o IP; lasciamo la validazione al livello di rete se necessario.
        String host = hostField.getText().trim();

        // Costruiamo il core di rete client con i parametri inseriti
        ClientCore core = new ClientCore(host, port);

        // Tentiamo il login remoto: se l'utente non esiste o la connessione fallisce, notifichiamo.
        try {
            if (!core.login(email)) {
                errorLabel.setText("Utente non esiste sul server.");
                return;
            }
        } catch (Exception ex) {
            // Messaggio dell'eccezione utile per diagnosi (es. timeout, refused, ecc.)
            errorLabel.setText("Connessione fallita: " + ex.getMessage());
            return;
        }

        // Prepara stato applicativo condiviso con la finestra principale
        ClientState state = new ClientState();
        state.userEmailProperty().set(email);

        // Carica la MainView e passa dipendenze + stato al relativo controller
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource("/MainView.fxml"));
            Scene scene = new Scene(l.load(), 900, 600);
            MainController controller = l.getController();
            controller.init(core, state); // iniezione dipendenze

            // Crea e mostra lo Stage principale
            Stage stage = new Stage();
            stage.setTitle("Mail Client - " + email);
            stage.setScene(scene);

            // Chiusura ordinata: delega al controller principale (es. stop polling, chiusure risorse)
            stage.setOnCloseRequest(ev -> controller.shutdown());
            stage.show();

            // Chiude la finestra di login (Stage corrente ricavato da un nodo della scena)
            ((Stage) emailField.getScene().getWindow()).close();

        } catch (Exception e) {
            // Possibili cause: FXML mancante/corrotto, errori runtime in init, ecc.
            errorLabel.setText("Errore apertura finestra principale: " + e.getMessage());
        }
    }
}
