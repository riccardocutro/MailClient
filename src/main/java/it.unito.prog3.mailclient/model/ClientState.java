package it.unito.prog3.mailclient.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ClientState {
    private final StringProperty userEmail = new SimpleStringProperty("");
    private final IntegerProperty lastId = new SimpleIntegerProperty(0);
    private final ObservableList<InboxItem> inbox = FXCollections.observableArrayList();

    public StringProperty userEmailProperty() { return userEmail; }
    public IntegerProperty lastIdProperty() { return lastId; }
    public ObservableList<InboxItem> inbox() { return inbox; }
}
