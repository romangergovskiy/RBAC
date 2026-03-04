package com.rbac.manager;

import com.rbac.model.Permission;
import com.rbac.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoleManagerTest {
    RoleManager manager;

    @BeforeEach
    void setUp() {
        manager = new RoleManager();
    }

    @Test
    void addAndFindByName() {
        Role r = new Role("Admin", "Admin role");
        manager.add(r);
        assertTrue(manager.findByName("Admin").isPresent());
        assertEquals(r.getId(), manager.findByName("Admin").get().getId());
    }

    @Test
    void addPermissionToRole() {
        Role r = new Role("Editor", "Editor");
        manager.add(r);
        manager.addPermissionToRole("Editor", new Permission("WRITE", "docs", "Write docs"));
        assertTrue(r.hasPermission("WRITE", "docs"));
    }

    @Test
    void findRolesWithPermission() {
        Role r = new Role("R", "R");
        r.addPermission(new Permission("READ", "files", "Read"));
        manager.add(r);
        assertEquals(1, manager.findRolesWithPermission("READ", "files").size());
    }
}
