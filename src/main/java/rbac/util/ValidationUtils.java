package rbac.util;

import java.util.regex.Pattern;

public final class ValidationUtils {
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern DATE_ONLY = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern DATE_TIME = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}(:\\d{2})?$");

    private ValidationUtils() {}

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME.matcher(username).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    public static boolean isValidDate(String date) {
        if (date == null || date.isBlank()) return false;
        String t = date.trim();
        return DATE_ONLY.matcher(t).matches() || DATE_TIME.matcher(t).matches();
    }

    public static String normalizeString(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("\\s+", " ");
    }

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must be non-empty");
        }
    }
}
