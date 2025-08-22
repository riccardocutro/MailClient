package it.unito.prog3.mailclient.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.util.List;

public class InboxItem {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty from = new SimpleStringProperty();
    private final StringProperty subject = new SimpleStringProperty();
    private final StringProperty body = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> date = new SimpleObjectProperty<>();
    private final List<String> to;

    public InboxItem(int id, String from, List<String> to, String subject, String body, LocalDateTime date) {
        this.id.set(id);
        this.from.set(from);
        this.to = List.copyOf(to);
        this.subject.set(subject);
        this.body.set(body);
        this.date.set(date);
    }
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public String getFrom() { return from.get(); }
    public StringProperty fromProperty() { return from; }
    public List<String> getTo() { return to; }
    public String getSubject() { return subject.get(); }
    public StringProperty subjectProperty() { return subject; }
    public String getBody() { return body.get(); }
    public StringProperty bodyProperty() { return body; }
    public LocalDateTime getDate() { return date.get(); }
    public ObjectProperty<LocalDateTime> dateProperty() { return date; }

    public String getPreview() {
        var b = body.get();
        return b == null ? "" : (b.length() > 200 ? b.substring(0,200) + "…" : b);
    }
}
