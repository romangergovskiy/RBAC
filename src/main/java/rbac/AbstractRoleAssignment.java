package rbac;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment {
    private final String assignmentId;
    private final User user;
    private final Role role;
    private final AssignmentMetadata metadata;

    protected AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        if (user == null) throw new IllegalArgumentException("user must not be null");
        if (role == null) throw new IllegalArgumentException("role must not be null");
        if (metadata == null) throw new IllegalArgumentException("metadata must not be null");
        this.assignmentId = UUID.randomUUID().toString();
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    @Override
    public String assignmentId() {
        return assignmentId;
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public AssignmentMetadata metadata() {
        return metadata;
    }

    @Override
    public abstract boolean isActive();

    @Override
    public abstract String assignmentType();

    public String summary() {
        String status = isActive() ? "ACTIVE" : "INACTIVE";
        String reason = metadata().reason();
        String reasonText = (reason == null || reason.isBlank()) ? "N/A" : reason;
        return "[" + assignmentType() + "] " + role().getName() + " assigned to " + user().username()
            + " by " + metadata().assignedBy() + " at " + metadata().assignedAt() + "\n"
            + "Reason: " + reasonText + "\n"
            + "Status: " + status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractRoleAssignment that)) return false;
        return assignmentId.equals(that.assignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }
}

