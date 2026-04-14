package rbac.manager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import rbac.filters.RoleFilter;
import rbac.model.Permission;
import rbac.model.Role;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> rolesById = new HashMap<>();
    private final Map<String, Role> rolesByName = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void add(Role item) {
        if (item == null) {
            throw new IllegalArgumentException("role is null");
        }
        String id = item.getId();
        String name = item.getName();
        lock.writeLock().lock();
        try {
            if (rolesById.containsKey(id)) {
                throw new IllegalStateException("Role with id '" + id + "' already exists");
            }
            if (rolesByName.containsKey(name)) {
                throw new IllegalStateException("Role with name '" + name + "' already exists");
            }
            rolesById.put(id, item);
            rolesByName.put(name, item);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean remove(Role item) {
        if (item == null) return false;
        lock.writeLock().lock();
        try {
            Role removed = rolesById.remove(item.getId());
            if (removed != null) {
                rolesByName.remove(removed.getName());
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<Role> findById(String id) {
        if (id == null) return Optional.empty();
        lock.readLock().lock();
        try {
            return Optional.ofNullable(rolesById.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Role> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(rolesById.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int count() {
        lock.readLock().lock();
        try {
            return rolesById.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            rolesById.clear();
            rolesByName.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<Role> findByName(String name) {
        if (name == null) return Optional.empty();
        lock.readLock().lock();
        try {
            return Optional.ofNullable(rolesByName.get(name));
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Role> findByFilter(RoleFilter filter) {
        List<Role> snapshot;
        lock.readLock().lock();
        try {
            snapshot = new ArrayList<>(rolesById.values());
        } finally {
            lock.readLock().unlock();
        }
        List<Role> result = new ArrayList<>();
        for (Role r : snapshot) {
            if (filter.test(r)) {
                result.add(r);
            }
        }
        return result;
    }

    public List<Role> findByFilterParallel(RoleFilter filter) {
        List<Role> snapshot;
        lock.readLock().lock();
        try {
            snapshot = new ArrayList<>(rolesById.values());
        } finally {
            lock.readLock().unlock();
        }
        return snapshot.parallelStream().filter(filter::test).toList();
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        List<Role> result = findByFilter(filter);
        result.sort(sorter);
        return result;
    }

    public boolean exists(String name) {
        lock.readLock().lock();
        try {
            return rolesByName.containsKey(name);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        lock.writeLock().lock();
        try {
            Role role = rolesByName.get(roleName);
            if (role == null) {
                throw new IllegalStateException("Role '" + roleName + "' does not exist");
            }
            role.addPermission(permission);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        lock.writeLock().lock();
        try {
            Role role = rolesByName.get(roleName);
            if (role == null) {
                throw new IllegalStateException("Role '" + roleName + "' does not exist");
            }
            role.removePermission(permission);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        List<Role> snapshot;
        lock.readLock().lock();
        try {
            snapshot = new ArrayList<>(rolesById.values());
        } finally {
            lock.readLock().unlock();
        }
        List<Role> result = new ArrayList<>();
        for (Role r : snapshot) {
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
        lock.readLock().lock();
        that.lock.readLock().lock();
        try {
            return Objects.equals(rolesById, that.rolesById) && Objects.equals(rolesByName, that.rolesByName);
        } finally {
            that.lock.readLock().unlock();
            lock.readLock().unlock();
        }
    }

    @Override
    public int hashCode() {
        lock.readLock().lock();
        try {
            return Objects.hash(rolesById, rolesByName);
        } finally {
            lock.readLock().unlock();
        }
    }
}

