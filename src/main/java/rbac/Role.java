package rbac;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class Role {
    private static final AtomicLong COUNTER = new AtomicLong(0);

    private final String id;
    private String name;
    private String description;
    private final Set<Permission> permissions;

    public Role(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Role name must be non-empty");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Role description must be non-empty");
        }
        this.id = "role_" + COUNTER.incrementAndGet();
        this.name = name;
        this.description = description;
        this.permissions = new HashSet<>();
    }

    public Role(String id, String name, String description, Set<Permission> permissions) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Role id must be non-empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Role name must be non-empty");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Role description must be non-empty");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.permissions = new HashSet<>(permissions == null ? Collections.emptySet() : permissions);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void addPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("permission must not be null");
        }
        permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        if (permission == null) {
            return;
        }
        permissions.remove(permission);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource) {
        return permissions.stream().anyMatch(p -> p.matches(permissionName, resource));
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(new HashSet<>(permissions));
    }

    public String format() {
        return "Role: " + name + " [ID: " + id + "]\n"
            + "Description: " + description + "\n"
            + "Permissions (" + permissions.size() + "):\n"
            + permissions.stream()
                .sorted((a, b) -> a.format().compareToIgnoreCase(b.format()))
                .map(p -> "- " + p.format())
                .reduce("", (acc, line) -> acc.isEmpty() ? line : acc + "\n" + line);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return id.equals(role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Role{name='" + name + "', id='" + id + "', permissions=" + permissions.size() + "}";
    }
}

