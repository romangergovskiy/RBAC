package com.rbac.filter;

import com.rbac.model.Role;

import java.util.Comparator;

public final class RoleSorters {
    private RoleSorters() {}

    public static Comparator<Role> byName() {
        return Comparator.comparing(Role::getName);
    }

    public static Comparator<Role> byPermissionCount() {
        return Comparator.comparingInt(r -> r.getPermissions().size());
    }
}
