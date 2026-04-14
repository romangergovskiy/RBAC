package rbac.manager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import rbac.filters.AssignmentFilter;
import rbac.filters.AssignmentFilters;
import rbac.model.Permission;
import rbac.model.PermanentAssignment;
import rbac.model.Role;
import rbac.model.RoleAssignment;
import rbac.model.TemporaryAssignment;
import rbac.model.User;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final Map<String, RoleAssignment> assignments = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void add(RoleAssignment item) {
        if (item == null) {
            throw new IllegalArgumentException("assignment is null");
        }
        String id = item.assignmentId();
        lock.writeLock().lock();
        try {
            if (assignments.containsKey(id)) {
                throw new IllegalStateException("Assignment with id '" + id + "' already exists");
            }
            RoleAssignment existingActive = findActiveAssignmentLocked(item.user(), item.role());
            if (existingActive != null) {
                throw new IllegalStateException("User already has active assignment for this role");
            }
            assignments.put(id, item);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private RoleAssignment findActiveAssignmentLocked(User user, Role role) {
        for (RoleAssignment a : assignments.values()) {
            if (a.user().equals(user) && a.role().equals(role) && a.isActive()) {
                return a;
            }
        }
        return null;
    }

    @Override
    public boolean remove(RoleAssignment item) {
        if (item == null) return false;
        lock.writeLock().lock();
        try {
            return assignments.remove(item.assignmentId(), item);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        if (id == null) return Optional.empty();
        lock.readLock().lock();
        try {
            return Optional.ofNullable(assignments.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<RoleAssignment> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(assignments.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int count() {
        lock.readLock().lock();
        try {
            return assignments.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            assignments.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<RoleAssignment> findByUser(User user) {
        return findByFilter(AssignmentFilters.byUser(user));
    }

    public List<RoleAssignment> findByRole(Role role) {
        return findByFilter(AssignmentFilters.byRole(role));
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        List<RoleAssignment> snapshot;
        lock.readLock().lock();
        try {
            snapshot = new ArrayList<>(assignments.values());
        } finally {
            lock.readLock().unlock();
        }
        List<RoleAssignment> result = new ArrayList<>();
        for (RoleAssignment a : snapshot) {
            if (filter.test(a)) {
                result.add(a);
            }
        }
        return result;
    }

    public List<RoleAssignment> findByFilterParallel(AssignmentFilter filter) {
        List<RoleAssignment> snapshot = new ArrayList<>(assignments.values());
        return snapshot.parallelStream().filter(filter::test).toList();
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        List<RoleAssignment> result = findByFilter(filter);
        result.sort(sorter);
        return result;
    }

    public List<RoleAssignment> getActiveAssignments() {
        return findByFilter(AssignmentFilters.activeOnly());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return findByFilter(a -> a instanceof TemporaryAssignment ta && ta.isExpired());
    }

    public boolean userHasRole(User user, Role role) {
        return findByUser(user).stream().anyMatch(RoleAssignment::isActive);
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        return getUserPermissions(user).stream().anyMatch(p -> p.matches(permissionName, resource));
    }

    public Set<Permission> getUserPermissions(User user) {
        List<RoleAssignment> snapshot;
        lock.readLock().lock();
        try {
            snapshot = new ArrayList<>(assignments.values());
        } finally {
            lock.readLock().unlock();
        }
        Set<Permission> result = new HashSet<>();
        for (RoleAssignment a : snapshot) {
            if (a.user().equals(user) && a.isActive()) {
                result.addAll(a.role().getPermissions());
            }
        }
        return result;
    }

    public void revokeAssignment(String assignmentId) {
        lock.writeLock().lock();
        try {
            RoleAssignment a = assignments.get(assignmentId);
            if (a == null) {
                throw new IllegalStateException("Assignment not found: " + assignmentId);
            }
            if (a instanceof PermanentAssignment pa) {
                pa.revoke();
            } else if (a instanceof TemporaryAssignment ta) {
                ta.extend(java.time.LocalDate.now().minusDays(1).toString());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        lock.writeLock().lock();
        try {
            RoleAssignment a = assignments.get(assignmentId);
            if (!(a instanceof TemporaryAssignment ta)) {
                throw new IllegalStateException("Assignment is not temporary: " + assignmentId);
            }
            ta.extend(newExpirationDate);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssignmentManager that)) return false;
        lock.readLock().lock();
        that.lock.readLock().lock();
        try {
            return Objects.equals(assignments, that.assignments);
        } finally {
            that.lock.readLock().unlock();
            lock.readLock().unlock();
        }
    }

    @Override
    public int hashCode() {
        lock.readLock().lock();
        try {
            return Objects.hash(assignments);
        } finally {
            lock.readLock().unlock();
        }
    }
}

