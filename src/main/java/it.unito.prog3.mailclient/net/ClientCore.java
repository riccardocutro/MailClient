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
    private final String host;
    private final int port;

    public ClientCore(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean login(String email) throws IOException {
        try (Wire w = new Wire(host, port)) {
            w.send(String.join(";", Protocol.CMD_LOGIN, email));
            return "OK".equals(w.receive());
        }
    }

    public void send(String from, List<String> to, String subject, String body) throws IOException {
        try (Wire w = new Wire(host, port)) {
            String cmd = String.join(";", Protocol.CMD_SEND, from, String.join(",", to),
                    Wire.b64(subject), Wire.b64(body));
            w.send(cmd);
            String resp = w.receive();
            if (!"OK".equals(resp)) throw new IOException("SEND failed: " + resp);
        }
    }

    public List<Email> getSince(String user, int lastId) throws IOException {
        try (Wire w = new Wire(host, port)) {
            w.send(String.join(";", Protocol.CMD_GET, user, String.valueOf(lastId)));
            var lines = w.receiveUntilEnd(); // MSG;...;END
            List<Email> out = new ArrayList<>();
            for (String line : lines) {
                if (!line.startsWith("MSG;")) continue;
                String[] p = line.split(";", 7);
                int id = Integer.parseInt(p[1]);
                String from = p[2];
                List<String> to = List.of(p[3].split(","));
                String subject = Wire.unb64(p[4]);
                String body = Wire.unb64(p[5]);
                long epoch = Long.parseLong(p[6]);
                LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
                out.add(new Email(id, from, to, subject, body, date));
            }
            return out;
        }
    }

    public boolean delete(String user, int id) throws IOException {
        try (Wire w = new Wire(host, port)) {
            w.send(String.join(";", Protocol.CMD_DELETE, user, String.valueOf(id)));
            return "OK".equals(w.receive());
        }
    }
}
