package com.rbac.filter;

import com.rbac.model.User;

public final class UserFilters {
    private UserFilters() {}

    public static UserFilter byUsername(String username) {
        return u -> u != null && u.username().equals(username);
    }

    public static UserFilter byUsernameContains(String substring) {
        if (substring == null) return u -> false;
        String s = substring.toLowerCase();
        return u -> u != null && u.username().toLowerCase().contains(s);
    }

    public static UserFilter byEmail(String email) {
        return u -> u != null && u.email().equals(email);
    }

    public static UserFilter byEmailDomain(String domain) {
        if (domain == null) return u -> false;
        String d = domain.startsWith("@") ? domain.toLowerCase() : "@" + domain.toLowerCase();
        return u -> u != null && u.email().toLowerCase().endsWith(d);
    }

    public static UserFilter byFullNameContains(String substring) {
        if (substring == null) return u -> false;
        String s = substring.toLowerCase();
        return u -> u != null && u.fullName().toLowerCase().contains(s);
    }
}
