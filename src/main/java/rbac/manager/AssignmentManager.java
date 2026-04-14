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

    @Override
    public void add(RoleAssignment item) {
        if (item == null) {
            throw new IllegalArgumentException("assignment is null");
        }
        String id = item.assignmentId();
        if (assignments.containsKey(id)) {
            throw new IllegalStateException("Assignment with id '" + id + "' already exists");
        }
        RoleAssignment existingActive = findActiveAssignment(item.user(), item.role());
        if (existingActive != null) {
            throw new IllegalStateException("User already has active assignment for this role");
        }
        assignments.put(id, item);
    }

    private RoleAssignment findActiveAssignment(User user, Role role) {
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
        return assignments.remove(item.assignmentId(), item);
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(assignments.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignments.values());
    }

    @Override
    public int count() {
        return assignments.size();
    }

    @Override
    public void clear() {
        assignments.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        return findByFilter(AssignmentFilters.byUser(user));
    }

    public List<RoleAssignment> findByRole(Role role) {
        return findByFilter(AssignmentFilters.byRole(role));
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        List<RoleAssignment> result = new ArrayList<>();
        for (RoleAssignment a : assignments.values()) {
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
        Set<Permission> result = new HashSet<>();
        for (RoleAssignment a : assignments.values()) {
            if (a.user().equals(user) && a.isActive()) {
                result.addAll(a.role().getPermissions());
            }
        }
        return result;
    }

    public void revokeAssignment(String assignmentId) {
        RoleAssignment a = assignments.get(assignmentId);
        if (a == null) {
            throw new IllegalStateException("Assignment not found: " + assignmentId);
        }
        if (a instanceof PermanentAssignment pa) {
            pa.revoke();
        } else if (a instanceof TemporaryAssignment ta) {
            ta.extend(java.time.LocalDate.now().minusDays(1).toString());
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        RoleAssignment a = assignments.get(assignmentId);
        if (!(a instanceof TemporaryAssignment ta)) {
            throw new IllegalStateException("Assignment is not temporary: " + assignmentId);
        }
        ta.extend(newExpirationDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AssignmentManager that)) return false;
        return Objects.equals(assignments, that.assignments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignments);
    }
}

