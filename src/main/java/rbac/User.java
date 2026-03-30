package rbac;

import java.util.regex.Pattern;

public record User(String username, String fullName, String email) {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public User {
        if (username == null || username.isBlank() || !USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("username must match [A-Za-z0-9_]{3,20}");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName must be a non-empty string");
        }
        if (email == null || email.isBlank() || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("email must be a valid email format");
        }
    }

    public static User create(String username, String fullName, String email) {
        // В record конструктора уже есть валидация.
        return new User(username, fullName, email);
    }

    public String format() {
        return username + " (" + fullName + ")\n<" + email + ">";
    }
}

