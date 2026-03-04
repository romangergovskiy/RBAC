package com.rbac.assignment;

import com.rbac.model.Role;
import com.rbac.model.User;

public interface RoleAssignment {
    String assignmentId();
    User user();
    Role role();
    AssignmentMetadata metadata();
    boolean isActive();
    String assignmentType();
}
