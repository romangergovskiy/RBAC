package com.rbac.filter;

import com.rbac.model.Permission;
import com.rbac.model.Role;

public final class RoleFilters {
    private RoleFilters() {}

    public static RoleFilter byName(String name) {
        return r -> r != null && r.getName().equals(name);
    }

    public static RoleFilter byNameContains(String substring) {
        if (substring == null) return r -> false;
        String s = substring.toLowerCase();
        return r -> r != null && r.getName().toLowerCase().contains(s);
    }

    public static RoleFilter hasPermission(Permission permission) {
        return r -> r != null && permission != null && r.hasPermission(permission);
    }

    public static RoleFilter hasPermission(String permissionName, String resource) {
        return r -> r != null && r.hasPermission(permissionName, resource);
    }

    public static RoleFilter hasAtLeastNPermissions(int n) {
        return r -> r != null && r.getPermissions().size() >= n;
    }
}
