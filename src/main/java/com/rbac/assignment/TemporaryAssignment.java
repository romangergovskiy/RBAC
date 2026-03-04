package com.rbac.assignment;

import com.rbac.model.Role;
import com.rbac.model.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private boolean autoRenew;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt, boolean autoRenew) {
        super(user, role, metadata);
        this.expiresAt = expiresAt;
        this.autoRenew = autoRenew;
    }

    @Override
    public boolean isActive() { return !isExpired(); }

    @Override
    public String assignmentType() { return "TEMPORARY"; }

    public void extend(String newExpirationDate) { this.expiresAt = newExpirationDate; }

    public boolean isExpired() {
        try {
            return LocalDateTime.now().isAfter(LocalDateTime.parse(expiresAt, FORMATTER));
        } catch (Exception e) { return true; }
    }

    public String getTimeRemaining() {
        if (isExpired()) return "Expired";
        try {
            var end = LocalDateTime.parse(expiresAt, FORMATTER);
            var d = java.time.Duration.between(LocalDateTime.now(), end);
            return String.format("%d days, %d hours, %d minutes", d.toDays(), d.toHours() % 24, d.toMinutes() % 60);
        } catch (Exception e) { return "Invalid date"; }
    }

    public String getExpiresAt() { return expiresAt; }
    public boolean isAutoRenew() { return autoRenew; }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.summary());
        sb.append(String.format("\nExpires at: %s", expiresAt));
        if (!isExpired()) sb.append(" (remaining: ").append(getTimeRemaining()).append(")");
        return sb.toString();
    }
}
