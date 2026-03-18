package rbac;

import rbac.model.AssignmentMetadata;
import rbac.model.PermanentAssignment;
import rbac.model.Role;
import rbac.model.TemporaryAssignment;
import rbac.model.User;
import rbac.model.Permission;

public class Main {
    public static void main(String[] args) {
        assertOkUser("john_doe", "John Doe", "john@example.com");

        assertThrows(() -> User.create("ab", "Bad Name", "bad@example.com"));
        assertThrows(() -> User.create("no spaces", "Bad Name", "bad@example.com"));
        assertThrows(() -> User.create("valid_user", "Bad Name", "bad-email"));

        Permission p = new Permission("read", "users", "Can view user list");
        if (!p.name().equals("READ")) throw new AssertionError("Permission name normalization failed");
        if (!p.resource().equals("users")) throw new AssertionError("Permission resource normalization failed");
        if (!p.format().contains("READ on users")) throw new AssertionError("Permission.format mismatch");
        if (!p.matches("read", "users")) throw new AssertionError("Permission.matches failed");
        if (p.matches("write", "users")) throw new AssertionError("Permission.matches should be false");

        assertThrows(() -> new Permission("read", "users", "   "));
        assertThrows(() -> new Permission("read write", "users", "desc"));
        assertThrows(() -> new Permission("read", "USERS", "desc"));

        Role role = new Role("Administrator", "Full system access");
        if (role.getId() == null || role.getId().isBlank()) throw new AssertionError("Role id not generated");
        role.addPermission(new Permission("READ", "users", "Can view user list"));
        role.addPermission(new Permission("WRITE", "users", "Can create and edit users"));
        role.addPermission(new Permission("DELETE", "users", "Can delete users"));

        if (!role.hasPermission(new Permission("READ", "users", "Can view user list"))) {
            throw new AssertionError("Role.hasPermission(Permission) should be true");
        }
        if (!role.hasPermission("read", "users")) throw new AssertionError("Role.hasPermission(String,String) should be true");
        int sizeBefore = role.getPermissions().size();
        role.removePermission(new Permission("DELETE", "users", "Can delete users"));
        if (role.getPermissions().size() == sizeBefore) throw new AssertionError("Role.removePermission failed");
        if (role.getPermissions().contains(null)) throw new AssertionError("Permissions should not contain null");

        String roleText = role.format();
        if (!roleText.contains("Role:")) throw new AssertionError("Role.format mismatch");

        AssignmentMetadata meta = AssignmentMetadata.now("john_doe", "Initial setup");
        if (meta.assignedBy() == null || meta.assignedBy().isBlank()) throw new AssertionError("AssignmentMetadata assignedBy missing");
        if (meta.assignedAt() == null || meta.assignedAt().isBlank()) throw new AssertionError("AssignmentMetadata assignedAt missing");
        if (meta.format() == null || meta.format().isBlank()) throw new AssertionError("AssignmentMetadata.format missing");

        User u = User.create("admin_1", "Admin One", "admin1@example.com");
        Role adminRole = new Role("AdminRole", "Test role");
        AssignmentMetadata m2 = AssignmentMetadata.now("admin_1", "Reason");
        PermanentAssignment perm = new PermanentAssignment(u, adminRole, m2);
        if (!perm.summary().contains("[PERMANENT]")) throw new AssertionError("Permanent summary type mismatch");
        if (!perm.summary().contains("Status:")) throw new AssertionError("Permanent summary status missing");
        PermanentAssignment perm2 = new PermanentAssignment(u, adminRole, m2);
        if (perm.equals(perm2)) throw new AssertionError("Different assignmentId => not equal");

        if (!perm.isActive()) throw new AssertionError("PermanentAssignment should be active by default");
        perm.revoke();
        if (perm.isActive()) throw new AssertionError("PermanentAssignment should be inactive after revoke");
        if (!perm.isRevoked()) throw new AssertionError("PermanentAssignment isRevoked mismatch");

        String future = java.time.LocalDate.now().plusDays(10).toString();
        String past = java.time.LocalDate.now().minusDays(10).toString();
        TemporaryAssignment tmpFuture = new TemporaryAssignment(u, adminRole, m2, future, false);
        if (!tmpFuture.isActive()) throw new AssertionError("TemporaryAssignment future should be active");
        if (tmpFuture.isExpired()) throw new AssertionError("TemporaryAssignment future should not be expired");
        tmpFuture.extend(java.time.LocalDate.now().plusDays(20).toString());
        if (!tmpFuture.isActive()) throw new AssertionError("TemporaryAssignment should stay active after extend");
        TemporaryAssignment tmpPast = new TemporaryAssignment(u, adminRole, m2, past, false);
        if (tmpPast.isActive()) throw new AssertionError("TemporaryAssignment past should be inactive");
        if (!tmpPast.isExpired()) throw new AssertionError("TemporaryAssignment past should be expired");

        System.out.println("1.1-1.7 All checks: OK");
    }

    private static void assertOkUser(String username, String fullName, String email) {
        User u = User.create(username, fullName, email);
        String formatted = u.format();
        if (!formatted.contains(username) || !formatted.contains(fullName) || !formatted.contains(email)) {
            throw new AssertionError("format() output mismatch");
        }
    }

    private static void assertThrows(Runnable r) {
        try {
            r.run();
        } catch (IllegalArgumentException e) {
            return;
        }
        throw new AssertionError("Expected IllegalArgumentException");
    }
}

