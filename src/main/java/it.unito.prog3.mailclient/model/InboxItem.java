package it.unito.prog3.mailclient.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.util.List;

public class InboxItem {
    // ID univoco del messaggio (usato per operazioni come eliminare o selezionare)
    private final IntegerProperty id = new SimpleIntegerProperty();

    // mittente del messaggio
    private final StringProperty from = new SimpleStringProperty();

    // oggetto del messaggio
    private final StringProperty subject = new SimpleStringProperty();

    // corpo completo del messaggio
    private final StringProperty body = new SimpleStringProperty();

    // data e ora di ricezione/invio
    private final ObjectProperty<LocalDateTime> date = new SimpleObjectProperty<>();

    // lista di destinatari (immutabile, non property)
    private final List<String> to;

    // costruttore: inizializza tutti i campi, copia la lista dei destinatari per sicurezza
    public InboxItem(int id, String from, List<String> to, String subject, String body, LocalDateTime date) {
        this.id.set(id);
        this.from.set(from);
        this.to = List.copyOf(to); // creiamo una copia per evitare modifiche esterne
        this.subject.set(subject);
        this.body.set(body);
        this.date.set(date);
    }

    // getter e property per ID (utile per binding con UI)
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }

    // getter e property per mittente
    public String getFrom() { return from.get(); }
    public StringProperty fromProperty() { return from; }

    // destinatari (solo getter, lista immutabile)
    public List<String> getTo() { return to; }

    // getter e property per oggetto
    public String getSubject() { return subject.get(); }
    public StringProperty subjectProperty() { return subject; }

    // getter e property per corpo
    public String getBody() { return body.get(); }
    public StringProperty bodyProperty() { return body; }

    // getter e property per data
    public LocalDateTime getDate() { return date.get(); }
    public ObjectProperty<LocalDateTime> dateProperty() { return date; }

    // ritorna un'anteprima del corpo (max 200 caratteri + "…" se lungo)
    public String getPreview() {
        var b = body.get();
        return b == null ? "" : (b.length() > 200 ? b.substring(0,200) + "…" : b);
    }
}
