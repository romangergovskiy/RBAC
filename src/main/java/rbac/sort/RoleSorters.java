package rbac.sort;

import java.util.Comparator;

import rbac.model.Role;

public final class RoleSorters {
    private RoleSorters() {
    }

    public static Comparator<Role> byName() {
        return Comparator.comparing(Role::getName, String.CASE_INSENSITIVE_ORDER);
    }

    public static Comparator<Role> byPermissionCount() {
        return Comparator.comparingInt(r -> r.getPermissions().size());
    }
}

