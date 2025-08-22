package it.unito.prog3.mailclient.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class EmailValidator {
    private EmailValidator(){}

    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public static boolean isValid(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    public static List<String> splitAndValidate(String csv) {
        var list = Arrays.stream(csv.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
        if (list.isEmpty() || !list.stream().allMatch(EmailValidator::isValid))
            throw new IllegalArgumentException("Indirizzo/i non validi.");
        return list;
    }
}
