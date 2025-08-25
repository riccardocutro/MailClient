package it.unito.prog3.mailclient.service;

import it.unito.prog3.mailclient.model.ClientState;
import it.unito.prog3.mailclient.model.InboxItem;
import it.unito.prog3.mailclient.net.ClientCore;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

public class PollingService extends ScheduledService<Void> {
    private final ClientCore core;   // componente di rete per comunicare col server
    private final ClientState state; // stato condiviso (inbox, ultimo id, ecc.)

    public PollingService(ClientCore core, ClientState state) {
        this.core = core;
        this.state = state;
        setPeriod(Duration.seconds(5));       // ogni 5 secondi fa polling
        setRestartOnFailure(true);            // se fallisce, riparte automaticamente
    }

    @Override
    protected Task<Void> createTask() {
        // ogni esecuzione di polling crea un Task separato
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                // chiede al server tutti i messaggi nuovi dopo l'ultimo id noto
                var msgs = core.getSince(state.userEmailProperty().get(), state.lastIdProperty().get());

                if (!msgs.isEmpty()) {
                    // aggiornamento della UI deve essere fatto nel thread JavaFX
                    Platform.runLater(() -> {
                        msgs.forEach(m -> {
                            // anteprima corpo (non usata qui, ma calcolata per eventuale logica)
                            var preview = m.getBody().length() > 80 ? m.getBody().substring(0,80) + "…" : m.getBody();

                            // aggiunge in cima (indice 0) il nuovo messaggio
                            state.inbox().add(0, new InboxItem(
                                    m.getId(),
                                    m.getFrom(),
                                    m.getTo(),
                                    m.getSubject(),
                                    m.getBody(),
                                    m.getDate()
                            ));

                            // aggiorna lastId se necessario
                            state.lastIdProperty().set(Math.max(state.lastIdProperty().get(), m.getId()));
                        });
                        // Notifica
                        if (!msgs.isEmpty()) {
                            // mittenti unici, massimo 3
                            var senders = msgs.stream()
                                    .map(m -> m.getFrom())
                                    .distinct()
                                    .limit(3)
                                    .toList();

                            String title = msgs.size() == 1 ? "Nuovo messaggio" : ("Nuovi messaggi: " + msgs.size());
                            String body;
                            if (senders.size() == 1) {
                                body = "Da: " + senders.get(0);
                            } else {
                                body = "Da: " + String.join(", ", senders) + (msgs.size() > senders.size() ? " ..." : "");
                            }

                            // Alert non bloccante
                            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, body) {{
                                setTitle(title);
                                setHeaderText(null);
                            }}.show();

                            // beep
                            try { java.awt.Toolkit.getDefaultToolkit().beep(); } catch (Throwable ignored) {}
                        }


                    });

                }
                return null;
            }
        };
    }
}
