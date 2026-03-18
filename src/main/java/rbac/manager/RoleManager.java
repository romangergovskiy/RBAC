package rbac.manager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import rbac.filters.RoleFilter;
import rbac.model.Permission;
import rbac.model.Role;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> rolesById = new HashMap<>();
    private final Map<String, Role> rolesByName = new HashMap<>();

    @Override
    public void add(Role item) {
        if (item == null) {
            throw new IllegalArgumentException("role is null");
        }
        String id = item.getId();
        String name = item.getName();
        if (rolesById.containsKey(id)) {
            throw new IllegalStateException("Role with id '" + id + "' already exists");
        }
        if (rolesByName.containsKey(name)) {
            throw new IllegalStateException("Role with name '" + name + "' already exists");
        }
        rolesById.put(id, item);
        rolesByName.put(name, item);
    }

    @Override
    public boolean remove(Role item) {
        if (item == null) return false;
        Role removed = rolesById.remove(item.getId());
        if (removed != null) {
            rolesByName.remove(removed.getName());
            return true;
        }
        return false;
    }

    @Override
    public Optional<Role> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(rolesById.get(id));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }

    @Override
    public int count() {
        return rolesById.size();
    }

    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    public Optional<Role> findByName(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(rolesByName.get(name));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        List<Role> result = new ArrayList<>();
        for (Role r : rolesById.values()) {
            if (filter.test(r)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        List<Role> result = findByFilter(filter);
        result.sort(sorter);
        return result;
    }

    public boolean exists(String name) {
        return rolesByName.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalStateException("Role '" + roleName + "' does not exist");
        }
        role.addPermission(permission);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        Role role = rolesByName.get(roleName);
        if (role == null) {
            throw new IllegalStateException("Role '" + roleName + "' does not exist");
        }
        role.removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        List<Role> result = new ArrayList<>();
        for (Role r : rolesById.values()) {
            if (r.hasPermission(permissionName, resource)) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleManager that)) return false;
        return Objects.equals(rolesById, that.rolesById) && Objects.equals(rolesByName, that.rolesByName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rolesById, rolesByName);
    }
}

