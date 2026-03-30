package rbac;

import java.util.Objects;

public record Permission(String name, String resource, String description) {
    public Permission {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Permission name must be non-empty");
        }
        if (name.contains(" ")) {
            throw new IllegalArgumentException("Permission name must not contain spaces");
        }

        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("Permission resource must be a non-empty string");
        }
        if (!resource.equals(resource.toLowerCase())) {
            throw new IllegalArgumentException("Permission resource must be lower case");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Permission description must be non-empty");
        }

        name = name.toUpperCase();
    }

    public String format() {
        return name + " on " + resource + ": " + description;
    }

    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameOk = (namePattern == null || namePattern.isBlank()) || name.contains(namePattern.toUpperCase());
        boolean resourceOk = (resourcePattern == null || resourcePattern.isBlank()) || resource.contains(resourcePattern.toLowerCase());
        return nameOk && resourceOk;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, resource, description);
    }
}

