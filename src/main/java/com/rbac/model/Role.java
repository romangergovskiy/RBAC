package com.rbac.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Role {
    private final String id;
    private String name;
    private String description;
    private final Set<Permission> permissions;

    public Role(String name, String description) {
        this.id = "role_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        this.name = name;
        this.description = description;
        this.permissions = new HashSet<>();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public void addPermission(Permission permission) {
        if (permission != null) permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource) {
        return permissions.stream()
                .anyMatch(p -> p.name().equalsIgnoreCase(permissionName) && p.resource().equalsIgnoreCase(resource));
    }

    public Set<Permission> getPermissions() {
        return Set.copyOf(permissions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return id.equals(role.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public String toString() {
        return String.format("Role{id='%s', name='%s', description='%s', permissions=%d}", id, name, description, permissions.size());
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Role: %s [ID: %s]\n", name, id));
        sb.append(String.format("Description: %s\n", description));
        sb.append(String.format("Permissions (%d):\n", permissions.size()));
        for (Permission perm : permissions) sb.append("- ").append(perm.format()).append("\n");
        return sb.toString();
    }
}
