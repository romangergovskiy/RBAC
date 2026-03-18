package rbac;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoleManagerTest {

    private Role newRole(String name) {
        return new Role(name, name + " description");
    }

    @Test
    void addAndFindByName() {
        RoleManager manager = new RoleManager();
        Role admin = newRole("Admin");
        manager.add(admin);

        assertEquals(1, manager.count());
        assertTrue(manager.findByName("Admin").isPresent());
    }

    @Test
    void duplicateNameOrIdThrows() {
        RoleManager manager = new RoleManager();
        Role r1 = newRole("Admin");
        manager.add(r1);

        assertThrows(IllegalStateException.class, () -> manager.add(r1));
    }

    @Test
    void filtersAndPermissions() {
        RoleManager manager = new RoleManager();
        Role admin = newRole("Admin");
        Role viewer = newRole("Viewer");
        manager.add(admin);
        manager.add(viewer);

        Permission readUsers = new Permission("READ", "users", "read users");
        Permission writeUsers = new Permission("WRITE", "users", "write users");

        manager.addPermissionToRole("Admin", readUsers);
        manager.addPermissionToRole("Admin", writeUsers);
        manager.addPermissionToRole("Viewer", readUsers);

        List<Role> withRead = manager.findRolesWithPermission("READ", "users");
        assertEquals(2, withRead.size());

        List<Role> admins = manager.findByFilter(RoleFilters.byName("Admin"));
        assertEquals(1, admins.size());

        List<Role> sorted = manager.findAll(r -> true, RoleSorters.byName());
        assertEquals("Admin", sorted.get(0).getName());

        manager.removePermissionFromRole("Viewer", readUsers);
        assertFalse(manager.findByName("Viewer").orElseThrow().hasPermission("READ", "users"));
    }
}

