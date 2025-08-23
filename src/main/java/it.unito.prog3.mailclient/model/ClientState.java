package it.unito.prog3.mailclient.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ClientState {
    // email dell'utente attualmente loggato (StringProperty per binding UI)
    private final StringProperty userEmail = new SimpleStringProperty("");

    // ultimo ID di messaggio visto (utile per polling/aggiornamenti)
    private final IntegerProperty lastId = new SimpleIntegerProperty(0);

    // lista osservabile di messaggi in inbox, usata per popolare la UI
    private final ObservableList<InboxItem> inbox = FXCollections.observableArrayList();

    // getter per proprietà email (usata per binding o get/set)
    public StringProperty userEmailProperty() { return userEmail; }

    // getter per proprietà lastId (usata per binding o get/set)
    public IntegerProperty lastIdProperty() { return lastId; }

    // restituisce la lista osservabile di messaggi (collegata alla ListView)
    public ObservableList<InboxItem> inbox() { return inbox; }
}
