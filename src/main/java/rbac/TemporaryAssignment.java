package rbac;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private String expiresAt;
    private boolean autoRenew;

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt, boolean autoRenew) {
        super(user, role, metadata);
        if (expiresAt == null || expiresAt.isBlank()) {
            throw new IllegalArgumentException("expiresAt must be non-empty");
        }
        this.expiresAt = expiresAt;
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
        return expiresAt.compareTo(java.time.LocalDate.now().toString()) < 0;
    }

    public String getTimeRemaining() {
        try {
            java.time.LocalDate exp = java.time.LocalDate.parse(expiresAt.substring(0, 10));
            java.time.LocalDate now = java.time.LocalDate.now();
            long days = java.time.temporal.ChronoUnit.DAYS.between(now, exp);
            return days + " day(s) remaining";
        } catch (Exception e) {
            return "unknown remaining time";
        }
    }
}

