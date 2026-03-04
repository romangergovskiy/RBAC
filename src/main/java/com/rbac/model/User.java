package com.rbac.model;

import java.util.regex.Pattern;

public record User(String username, String fullName, String email) {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@]+@[^@]+\\.[^@]+$");

    public User {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name cannot be null or empty");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Username must contain only latin letters, digits and underscore, 3-20 characters");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email must contain @ and dot after @");
        }
    }

    public static User create(String username, String fullName, String email) {
        return new User(username, fullName, email);
    }

    public String format() {
        return String.format("%s (%s) <%s>", username, fullName, email);
    }
}
