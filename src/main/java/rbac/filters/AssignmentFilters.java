package rbac.filters;

import rbac.model.AssignmentMetadata;
import rbac.model.Role;
import rbac.model.RoleAssignment;
import rbac.model.TemporaryAssignment;
import rbac.model.User;

public final class AssignmentFilters {
    private AssignmentFilters() {
    }

    public static AssignmentFilter byUser(User user) {
        return a -> a != null && a.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username) {
        return a -> a != null && a.user().username().equals(username);
    }

    public static AssignmentFilter byRole(Role role) {
        return a -> a != null && a.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName) {
        return a -> a != null && a.role().getName().equals(roleName);
    }

    public static AssignmentFilter activeOnly() {
        return a -> a != null && a.isActive();
    }

    public static AssignmentFilter inactiveOnly() {
        return a -> a != null && !a.isActive();
    }

    public static AssignmentFilter byType(String type) {
        return a -> a != null && a.assignmentType().equalsIgnoreCase(type);
    }

    public static AssignmentFilter assignedBy(String username) {
        return a -> a != null && a.metadata().assignedBy().equals(username);
    }

    public static AssignmentFilter assignedAfter(String date) {
        String threshold = date;
        return a -> a != null && a.metadata().assignedAt().compareTo(threshold) > 0;
    }

    public static AssignmentFilter expiringBefore(String date) {
        String threshold = date;
        return a -> {
            if (a == null || !"TEMPORARY".equals(a.assignmentType())) {
                return false;
            }
            if (!(a instanceof TemporaryAssignment ta)) {
                return false;
            }
            String expiresAt = ta.getExpiresAt();
            return expiresAt.compareTo(threshold) < 0;
        };
    }
}

