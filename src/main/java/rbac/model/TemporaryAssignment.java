package rbac.model;

import rbac.util.DateUtils;
import rbac.util.ValidationUtils;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private boolean autoRenew;

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt, boolean autoRenew) {
        super(user, role, metadata);
        ValidationUtils.requireNonEmpty(expiresAt, "expiresAt");
        if (!ValidationUtils.isValidDate(expiresAt.trim())) {
            throw new IllegalArgumentException("expiresAt must be YYYY-MM-DD or YYYY-MM-DD HH:mm");
        }
        this.expiresAt = ValidationUtils.normalizeString(expiresAt);
        this.autoRenew = autoRenew;
    }

    @Override
    public boolean isActive() {
        return !isExpired();
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public void extend(String newExpirationDate) {
        if (newExpirationDate == null || newExpirationDate.isBlank()) {
            throw new IllegalArgumentException("newExpirationDate must be non-empty");
        }
        this.expiresAt = newExpirationDate;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public boolean isExpired() {
        String today = DateUtils.getCurrentDate();
        String exp = expiresAt.length() >= 10 ? expiresAt.substring(0, 10) : expiresAt;
        return DateUtils.isBefore(exp, today);
    }

    public String getTimeRemaining() {
        return DateUtils.formatRelativeTime(expiresAt);
    }
}

