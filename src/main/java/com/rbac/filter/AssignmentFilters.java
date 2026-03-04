package com.rbac.filter;

import com.rbac.assignment.RoleAssignment;
import com.rbac.assignment.TemporaryAssignment;
import com.rbac.model.Role;
import com.rbac.model.User;

public final class AssignmentFilters {
    private AssignmentFilters() {}

    public static AssignmentFilter byUser(User user) {
        return a -> a != null && user != null && a.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username) {
        return a -> a != null && a.user().username().equals(username);
    }

    public static AssignmentFilter byRole(Role role) {
        return a -> a != null && role != null && a.role().equals(role);
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
        return a -> a != null && a.assignmentType().equals(type);
    }

    public static AssignmentFilter assignedBy(String username) {
        return a -> a != null && a.metadata().assignedBy().equals(username);
    }

    public static AssignmentFilter assignedAfter(String date) {
        return a -> a != null && a.metadata().assignedAt().compareTo(date) > 0;
    }

    public static AssignmentFilter expiringBefore(String date) {
        return a -> {
            if (a == null || !(a instanceof TemporaryAssignment t)) return false;
            String exp = t.getExpiresAt();
            return exp != null && exp.compareTo(date) < 0;
        };
    }
}
