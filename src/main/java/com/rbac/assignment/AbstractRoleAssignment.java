package com.rbac.assignment;

import com.rbac.model.Role;
import com.rbac.model.User;

import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment {
    protected final String assignmentId;
    protected final User user;
    protected final Role role;
    protected final AssignmentMetadata metadata;

    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        this.assignmentId = "assign_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    @Override public String assignmentId() { return assignmentId; }
    @Override public User user() { return user; }
    @Override public Role role() { return role; }
    @Override public AssignmentMetadata metadata() { return metadata; }
    @Override public abstract boolean isActive();
    @Override public abstract String assignmentType();

    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[%s] %s assigned to %s by %s at %s\n",
                assignmentType(), role.getName(), user.username(), metadata.assignedBy(), metadata.assignedAt()));
        if (metadata.reason() != null && !metadata.reason().isBlank())
            sb.append(String.format("Reason: %s\n", metadata.reason()));
        sb.append(String.format("Status: %s", isActive() ? "ACTIVE" : "INACTIVE"));
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractRoleAssignment that = (AbstractRoleAssignment) o;
        return assignmentId.equals(that.assignmentId);
    }

    @Override
    public int hashCode() { return assignmentId.hashCode(); }
}
