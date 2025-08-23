package it.unito.prog3.mailclient.net;

import shared.Email;
import shared.Protocol;
import shared.Wire;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class ClientCore {
    // host e porta del server di posta
    private final String host;
    private final int port;

    // costruttore: inizializza host e porta
    public ClientCore(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // Effettua il login: invia comando CMD_LOGIN e controlla la risposta "OK"
    public boolean login(String email) throws IOException {
        try (Wire w = new Wire(host, port)) {
            w.send(String.join(";", Protocol.CMD_LOGIN, email));
            return "OK".equals(w.receive());
        }
    }

    // Invia un'email al server
    public void send(String from, List<String> to, String subject, String body) throws IOException {
        try (Wire w = new Wire(host, port)) {
            // Comando formato: CMD_SEND;mittente;destinatari separati da virgola;subject codificato;body codificato
            String cmd = String.join(";", Protocol.CMD_SEND, from, String.join(",", to),
                    Wire.b64(subject), Wire.b64(body));
            w.send(cmd);
            String resp = w.receive();
            if (!"OK".equals(resp)) throw new IOException("SEND failed: " + resp);
        }
    }

    // Recupera tutti i messaggi successivi a lastId per l'utente indicato
    public List<Email> getSince(String user, int lastId) throws IOException {
        try (Wire w = new Wire(host, port)) {
            // Comando formato: CMD_GET;utente;ultimoId
            w.send(String.join(";", Protocol.CMD_GET, user, String.valueOf(lastId)));

            // Riceve righe fino a END. Ogni messaggio ha prefisso MSG;...
            var lines = w.receiveUntilEnd();
            List<Email> out = new ArrayList<>();
            for (String line : lines) {
                if (!line.startsWith("MSG;")) continue; // scarta righe non di messaggio
                String[] p = line.split(";", 7); // divide nei campi attesi
                int id = Integer.parseInt(p[1]);
                String from = p[2];
                List<String> to = List.of(p[3].split(","));
                String subject = Wire.unb64(p[4]);
                String body = Wire.unb64(p[5]);
                long epoch = Long.parseLong(p[6]);
                // converte timestamp in LocalDateTime UTC
                LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);

                // aggiunge un oggetto Email alla lista
                out.add(new Email(id, from, to, subject, body, date));
            }
            return out;
        }
    }

    // Elimina un messaggio dal server per l'utente
    public boolean delete(String user, int id) throws IOException {
        try (Wire w = new Wire(host, port)) {
            w.send(String.join(";", Protocol.CMD_DELETE, user, String.valueOf(id)));
            return "OK".equals(w.receive());
        }
    }
}
