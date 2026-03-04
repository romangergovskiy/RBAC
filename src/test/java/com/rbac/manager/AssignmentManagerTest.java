package com.rbac.manager;

import com.rbac.assignment.AssignmentMetadata;
import com.rbac.assignment.PermanentAssignment;
import com.rbac.assignment.RoleAssignment;
import com.rbac.model.Permission;
import com.rbac.model.Role;
import com.rbac.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentManagerTest {
    UserManager userManager;
    RoleManager roleManager;
    AssignmentManager assignmentManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
        roleManager = new RoleManager();
        assignmentManager = new AssignmentManager(userManager, roleManager);
        userManager.add(User.create("alice", "Alice", "alice@t.com"));
        Role admin = new Role("Admin", "Admin");
        admin.addPermission(new Permission("READ", "reports", "Read"));
        roleManager.add(admin);
    }

    @Test
    void addAndFindByUser() {
        User alice = userManager.findByUsername("alice").get();
        Role admin = roleManager.findByName("Admin").get();
        RoleAssignment a = new PermanentAssignment(alice, admin, AssignmentMetadata.now("sys", ""));
        assignmentManager.add(a);
        List<RoleAssignment> list = assignmentManager.findByUser(alice);
        assertEquals(1, list.size());
        assertEquals("Admin", list.get(0).role().getName());
    }

    @Test
    void userHasPermission() {
        User alice = userManager.findByUsername("alice").get();
        Role admin = roleManager.findByName("Admin").get();
        assignmentManager.add(new PermanentAssignment(alice, admin, AssignmentMetadata.now("sys", "")));
        assertTrue(assignmentManager.userHasPermission(alice, "READ", "reports"));
    }

    @Test
    void duplicateRoleThrows() {
        User alice = userManager.findByUsername("alice").get();
        Role admin = roleManager.findByName("Admin").get();
        assignmentManager.add(new PermanentAssignment(alice, admin, AssignmentMetadata.now("sys", "")));
        assertThrows(IllegalArgumentException.class, () ->
                assignmentManager.add(new PermanentAssignment(alice, admin, AssignmentMetadata.now("sys", "2"))));
    }
}
