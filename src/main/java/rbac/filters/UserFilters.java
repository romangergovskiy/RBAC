package rbac.filters;

import rbac.model.User;

public final class UserFilters {
    private UserFilters() {
    }

    public static UserFilter byUsername(String username) {
        return user -> user != null && user.username().equals(username);
    }

    public static UserFilter byUsernameContains(String substring) {
        String needle = substring == null ? "" : substring.toLowerCase();
        return user -> user != null && user.username().toLowerCase().contains(needle);
    }

    public static UserFilter byEmail(String email) {
        return user -> user != null && user.email().equals(email);
    }

    public static UserFilter byEmailDomain(String domain) {
        String d = domain == null ? "" : domain.toLowerCase();
        return user -> user != null && user.email().toLowerCase().endsWith(d);
    }

    public static UserFilter byFullNameContains(String substring) {
        String needle = substring == null ? "" : substring.toLowerCase();
        return user -> user != null && user.fullName().toLowerCase().contains(needle);
    }
}

