package com.rbac.manager;

import com.rbac.assignment.PermanentAssignment;
import com.rbac.assignment.RoleAssignment;
import com.rbac.assignment.TemporaryAssignment;
import com.rbac.filter.AssignmentFilter;
import com.rbac.model.Permission;
import com.rbac.model.Role;
import com.rbac.model.User;
import com.rbac.repository.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final Map<String, RoleAssignment> byId = new ConcurrentHashMap<>();
    private final UserManager userManager;
    private final RoleManager roleManager;

    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.userManager = userManager;
        this.roleManager = roleManager;
    }

    @Override
    public void add(RoleAssignment item) {
        if (item == null) throw new IllegalArgumentException("Assignment cannot be null");
        if (!userManager.exists(item.user().username())) throw new IllegalArgumentException("User not found: " + item.user().username());
        if (roleManager.findById(item.role().getId()).isEmpty()) throw new IllegalArgumentException("Role not found: " + item.role().getName());
        boolean duplicate = findByUser(item.user()).stream().anyMatch(a -> a.role().equals(item.role()) && a.isActive());
        if (duplicate) throw new IllegalArgumentException("User already has this role: " + item.role().getName());
        byId.put(item.assignmentId(), item);
    }

    @Override
    public boolean remove(RoleAssignment item) {
        return item != null && byId.remove(item.assignmentId(), item);
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(byId.values());
    }

    @Override
    public int count() {
        return byId.size();
    }

    @Override
    public void clear() {
        byId.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        return byId.values().stream().filter(a -> a.user().equals(user)).toList();
    }

    public List<RoleAssignment> findByRole(Role role) {
        return byId.values().stream().filter(a -> a.role().equals(role)).toList();
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        if (filter == null) return findAll();
        return byId.values().stream().filter(filter::test).toList();
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        List<RoleAssignment> list = filter == null ? findAll() : findByFilter(filter);
        if (sorter != null) {
            list = new ArrayList<>(list);
            list.sort(sorter);
        }
        return list;
    }

    public List<RoleAssignment> getActiveAssignments() {
        return byId.values().stream().filter(RoleAssignment::isActive).toList();
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return byId.values().stream().filter(a -> a instanceof TemporaryAssignment t && t.isExpired()).toList();
    }

    public boolean userHasRole(User user, Role role) {
        return findByUser(user).stream().anyMatch(a -> a.role().equals(role) && a.isActive());
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        return getUserPermissions(user).stream().anyMatch(p -> p.name().equalsIgnoreCase(permissionName) && p.resource().equalsIgnoreCase(resource));
    }

    public Set<Permission> getUserPermissions(User user) {
        Set<Permission> set = new HashSet<>();
        for (RoleAssignment a : findByUser(user)) {
            if (a.isActive()) set.addAll(a.role().getPermissions());
        }
        return set;
    }

    public void revokeAssignment(String assignmentId) {
        RoleAssignment a = byId.get(assignmentId);
        if (a == null) throw new IllegalArgumentException("Assignment not found: " + assignmentId);
        if (a instanceof PermanentAssignment p) p.revoke();
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        RoleAssignment a = byId.get(assignmentId);
        if (a == null) throw new IllegalArgumentException("Assignment not found: " + assignmentId);
        if (a instanceof TemporaryAssignment t) t.extend(newExpirationDate);
        else throw new IllegalArgumentException("Not a temporary assignment: " + assignmentId);
    }
}
