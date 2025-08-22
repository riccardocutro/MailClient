package it.unito.prog3.mailclient.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class InboxItem {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty from = new SimpleStringProperty();
    private final StringProperty subject = new SimpleStringProperty();
    private final StringProperty preview = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> date = new SimpleObjectProperty<>();

    public InboxItem(int id, String from, String subject, String preview, LocalDateTime date) {
        this.id.set(id); this.from.set(from); this.subject.set(subject);
        this.preview.set(preview); this.date.set(date);
    }
    public int getId() { return id.get(); }
    public IntegerProperty idProperty() { return id; }
    public String getFrom() { return from.get(); }
    public StringProperty fromProperty() { return from; }
    public String getSubject() { return subject.get(); }
    public StringProperty subjectProperty() { return subject; }
    public String getPreview() { return preview.get(); }
    public StringProperty previewProperty() { return preview; }
    public LocalDateTime getDate() { return date.get(); }
    public ObjectProperty<LocalDateTime> dateProperty() { return date; }
}
