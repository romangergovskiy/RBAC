package rbac.model;

import rbac.util.ValidationUtils;

public record User(String username, String fullName, String email) {

    public User {
        if (!ValidationUtils.isValidUsername(username)) {
            throw new IllegalArgumentException("username must match [A-Za-z0-9_]{3,20}");
        }
        ValidationUtils.requireNonEmpty(fullName, "fullName");
        if (!ValidationUtils.isValidEmail(email)) {
            throw new IllegalArgumentException("email must be a valid email format");
        }
    }

    public static User create(String username, String fullName, String email) {
        return new User(
            ValidationUtils.normalizeString(username),
            ValidationUtils.normalizeString(fullName),
            ValidationUtils.normalizeString(email)
        );
    }

    public String format() {
        return username + " (" + fullName + ")\n<" + email + ">";
    }
}

