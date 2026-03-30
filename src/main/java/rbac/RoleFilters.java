package rbac;

public final class RoleFilters {
    private RoleFilters() {
    }

    public static RoleFilter byName(String name) {
        return role -> role != null && role.getName().equals(name);
    }

    public static RoleFilter byNameContains(String substring) {
        String needle = substring == null ? "" : substring.toLowerCase();
        return role -> role != null && role.getName().toLowerCase().contains(needle);
    }

    public static RoleFilter hasPermission(Permission permission) {
        return role -> role != null && role.hasPermission(permission);
    }

    public static RoleFilter hasPermission(String permissionName, String resource) {
        return role -> role != null && role.hasPermission(permissionName, resource);
    }

    public static RoleFilter hasAtLeastNPermissions(int n) {
        return role -> role != null && role.getPermissions().size() >= n;
    }
}

