package com.rbac.manager;

import com.rbac.filter.RoleFilter;
import com.rbac.model.Permission;
import com.rbac.model.Role;
import com.rbac.repository.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> byId = new ConcurrentHashMap<>();
    private final Map<String, Role> byName = new ConcurrentHashMap<>();

    @Override
    public void add(Role item) {
        if (item == null) throw new IllegalArgumentException("Role cannot be null");
        if (byName.containsKey(item.getName())) throw new IllegalArgumentException("Role already exists: " + item.getName());
        byId.put(item.getId(), item);
        byName.put(item.getName(), item);
    }

    @Override
    public boolean remove(Role item) {
        if (item == null) return false;
        byName.remove(item.getName());
        return byId.remove(item.getId(), item);
    }

    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(byId.values());
    }

    @Override
    public int count() {
        return byId.size();
    }

    @Override
    public void clear() {
        byId.clear();
        byName.clear();
    }

    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(byName.get(name));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        if (filter == null) return findAll();
        return byId.values().stream().filter(filter::test).toList();
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        List<Role> list = filter == null ? findAll() : findByFilter(filter);
        if (sorter != null) {
            list = new ArrayList<>(list);
            list.sort(sorter);
        }
        return list;
    }

    public boolean exists(String name) {
        return name != null && byName.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        Role r = byName.get(roleName);
        if (r == null) throw new IllegalArgumentException("Role not found: " + roleName);
        r.addPermission(permission);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        Role r = byName.get(roleName);
        if (r == null) throw new IllegalArgumentException("Role not found: " + roleName);
        r.removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        return byId.values().stream().filter(r -> r.hasPermission(permissionName, resource)).toList();
    }
}
