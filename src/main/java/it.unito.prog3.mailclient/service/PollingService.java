package it.unito.prog3.mailclient.service;

import it.unito.prog3.mailclient.model.ClientState;
import it.unito.prog3.mailclient.model.InboxItem;
import it.unito.prog3.mailclient.net.ClientCore;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

public class PollingService extends ScheduledService<Void> {
    private final ClientCore core;
    private final ClientState state;

    public PollingService(ClientCore core, ClientState state) {
        this.core = core; this.state = state;
        setPeriod(Duration.seconds(5));
        setRestartOnFailure(true);
    }

    @Override protected Task<Void> createTask() {
        return new Task<>() {
            @Override protected Void call() throws Exception {
                var msgs = core.getSince(state.userEmailProperty().get(), state.lastIdProperty().get());
                if (!msgs.isEmpty()) {
                    Platform.runLater(() -> {
                        msgs.forEach(m -> {
                            var preview = m.getBody().length() > 80 ? m.getBody().substring(0,80)+"…" : m.getBody();
                            state.inbox().add(0, new InboxItem(m.getId(), m.getFrom(), m.getTo(), m.getSubject(), m.getBody(), m.getDate()));
                            state.lastIdProperty().set(Math.max(state.lastIdProperty().get(), m.getId()));
                        });
                    });
                }
                return null;
            }
        };
    }
}
