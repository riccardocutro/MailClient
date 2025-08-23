package it.unito.prog3.mailclient.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class EmailValidator {
    // classe utility: costruttore privato per evitare istanziazione
    private EmailValidator(){}

    // pattern base per validare un indirizzo email semplice (non copre tutti i casi RFC, ma sufficiente)
    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    // controlla se una singola email è valida (trim e match con il pattern)
    public static boolean isValid(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    // divide una stringa CSV di email (separate da virgola), pulisce spazi e valida tutte
    // lancia IllegalArgumentException se lista vuota o se almeno un indirizzo è invalido
    public static List<String> splitAndValidate(String csv) {
        var list = Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if (list.isEmpty() || !list.stream().allMatch(EmailValidator::isValid))
            throw new IllegalArgumentException("Indirizzo/i non validi.");

        return list;
    }
}
