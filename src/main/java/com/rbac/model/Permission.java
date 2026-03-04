package com.rbac.model;

public record Permission(String name, String resource, String description) {

    public Permission {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Permission name cannot be null or empty");
        }
        if (resource == null || resource.isBlank()) {
            throw new IllegalArgumentException("Resource cannot be null or empty");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        name = name.toUpperCase().trim();
        if (name.contains(" ")) {
            throw new IllegalArgumentException("Permission name cannot contain spaces");
        }
        resource = resource.toLowerCase().trim();
    }

    public String format() {
        return String.format("%s on %s: %s", name, resource, description);
    }

    public boolean matches(String namePattern, String resourcePattern) {
        if (namePattern == null) namePattern = "";
        if (resourcePattern == null) resourcePattern = "";
        boolean nameMatch = name.contains(namePattern.toUpperCase()) ||
                name.matches(".*" + namePattern.toUpperCase() + ".*");
        boolean resourceMatch = resource.contains(resourcePattern.toLowerCase()) ||
                resource.matches(".*" + resourcePattern.toLowerCase() + ".*");
        return nameMatch && resourceMatch;
    }
}
