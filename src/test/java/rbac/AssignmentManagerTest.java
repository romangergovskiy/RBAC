package rbac;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentManagerTest {

    private User user(String username) {
        return User.create(username, username + " Name", username + "@example.com");
    }

    private Role role(String name) {
        return new Role(name, name + " role");
    }

    @Test
    void addAndQueryAssignments() {
        AssignmentManager manager = new AssignmentManager();
        User u1 = user("userOne");
        Role r1 = role("Admin");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "initial");

        PermanentAssignment a1 = new PermanentAssignment(u1, r1, meta);
        manager.add(a1);

        assertEquals(1, manager.count());
        assertEquals(1, manager.findByUser(u1).size());
        assertTrue(manager.getActiveAssignments().contains(a1));
    }

    @Test
    void preventDuplicateActiveAssignments() {
        AssignmentManager manager = new AssignmentManager();
        User u1 = user("userTwo");
        Role r1 = role("Admin");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "initial");

        PermanentAssignment a1 = new PermanentAssignment(u1, r1, meta);
        manager.add(a1);

        PermanentAssignment a2 = new PermanentAssignment(u1, r1, meta);
        assertThrows(IllegalStateException.class, () -> manager.add(a2));
    }

    @Test
    void userPermissionsAggregation() {
        AssignmentManager manager = new AssignmentManager();
        User u1 = user("userThree");
        Role admin = role("Admin");
        Role viewer = role("Viewer");

        Permission readUsers = new Permission("READ", "users", "read users");
        Permission writeUsers = new Permission("WRITE", "users", "write users");

        admin.addPermission(readUsers);
        admin.addPermission(writeUsers);
        viewer.addPermission(readUsers);

        AssignmentMetadata meta = AssignmentMetadata.now("admin", "initial");

        PermanentAssignment a1 = new PermanentAssignment(u1, admin, meta);
        PermanentAssignment a2 = new PermanentAssignment(u1, viewer, meta);

        manager.add(a1);
        manager.add(a2);

        Set<Permission> perms = manager.getUserPermissions(u1);
        assertEquals(2, perms.size());
        assertTrue(manager.userHasPermission(u1, "READ", "users"));
        assertTrue(manager.userHasPermission(u1, "WRITE", "users"));
    }

    @Test
    void revokeAndExtendAssignments() {
        AssignmentManager manager = new AssignmentManager();
        User u1 = user("userFour");
        Role r1 = role("Admin");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "initial");

        PermanentAssignment permanent = new PermanentAssignment(u1, r1, meta);
        manager.add(permanent);

        User u2 = user("userFive");
        Role r2 = role("Viewer");
        TemporaryAssignment temp = new TemporaryAssignment(u2, r2, meta, java.time.LocalDate.now().plusDays(5).toString(), false);
        manager.add(temp);

        manager.revokeAssignment(permanent.assignmentId());
        assertFalse(permanent.isActive());

        manager.extendTemporaryAssignment(temp.assignmentId(), java.time.LocalDate.now().plusDays(10).toString());
        assertTrue(temp.isActive());
    }
}

