package shared;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class Email implements Serializable {

    private static final long serialVersionUID = 1L;
    // formatter ISO standard per serializzare/deserializzare la data
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // identificativo univoco del messaggio
    private final int id;
    // mittente
    private final String from;
    // lista di destinatari (può avere più elementi)
    private final List<String> to;
    // oggetto
    private final String subject;
    // corpo del messaggio
    private final String body;
    // data/ora di invio
    private final LocalDateTime date;

    // costruttore: destinatari in formato CSV (verranno splittati)
    public Email(int id, String from, String to, String subject, String body, LocalDateTime date) {
        this(id, from, Arrays.asList(to.split(",")), subject, body, date);
    }

    // costruttore: destinatari come lista
    public Email(int id, String from, List<String> to, String subject, String body, LocalDateTime date) {
        this.id = id;
        this.from = from;
        this.to = List.copyOf(to); // difensivo: crea copia immutabile
        this.subject = subject;
        this.body = body;
        this.date = date;
    }

    // getter per tutti i campi
    public int getId() { return id; }
    public String getFrom() { return from; }
    public List<String> getTo() { return to; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
    public LocalDateTime getDate() { return date; }

    // serializza l'email in stringa per il protocollo client-server
    @Override
    public String toString() {
        String toStr = String.join(",", to);
        return id + ";" + from + ";" + toStr + ";" + subject + ";" + body + ";" + date.format(DATE_FMT);
    }

    // crea un'istanza Email a partire da una stringa ricevuta dal socket
    public static Email fromString(String s) {
        try {
            String[] parts = s.split(";", 6);
            int id = Integer.parseInt(parts[0]);
            String from = parts[1];
            String to = parts[2];
            String subject = parts[3];
            String body = parts[4];
            LocalDateTime date = LocalDateTime.parse(parts[5], DATE_FMT);
            return new Email(id, from, to, subject, body, date);
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato Email non valido: " + s, e);
        }
    }

    // alias di getDate (solo per leggibilità)
    public LocalDateTime getSentAt() {
        return date;
    }
}
