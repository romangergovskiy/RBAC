package rbac;

import org.junit.jupiter.api.Test;

import java.util.List;

import rbac.filters.UserFilters;
import rbac.manager.UserManager;
import rbac.model.User;
import rbac.sort.UserSorters;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    @Test
    void addAndFindByUsername() {
        UserManager manager = new UserManager();
        User u = User.create("john", "John Doe", "john@example.com");
        manager.add(u);

        assertEquals(1, manager.count());
        assertTrue(manager.findByUsername("john").isPresent());
        assertEquals("john@example.com", manager.findByUsername("john").get().email());
    }

    @Test
    void addDuplicateUsernameThrows() {
        UserManager manager = new UserManager();
        manager.add(User.create("john", "John Doe", "john@example.com"));
        assertThrows(IllegalStateException.class, () ->
            manager.add(User.create("john", "John Smith", "john2@example.com"))
        );
    }

    @Test
    void findByEmailAndFilterAndSort() {
        UserManager manager = new UserManager();
        manager.add(User.create("userA", "Alice A", "alice@company.com"));
        manager.add(User.create("userB", "Bob B", "bob@company.com"));
        manager.add(User.create("userC", "Charlie C", "charlie@other.com"));

        assertTrue(manager.findByEmail("bob@company.com").isPresent());

        List<User> company = manager.findByFilter(UserFilters.byEmailDomain("@company.com"));
        assertEquals(2, company.size());

        List<User> sorted = manager.findAll(UserFilters.byEmailDomain("@company.com"), UserSorters.byUsername());
        assertEquals("userA", sorted.get(0).username());
        assertEquals("userB", sorted.get(1).username());
    }

    @Test
    void updateUser() {
        UserManager manager = new UserManager();
        manager.add(User.create("john", "John Doe", "john@example.com"));

        manager.update("john", "John Updated", "john.updated@example.com");

        User updated = manager.findByUsername("john").orElseThrow();
        assertEquals("John Updated", updated.fullName());
        assertEquals("john.updated@example.com", updated.email());
    }

    @Test
    void updateNonExistingUserThrows() {
        UserManager manager = new UserManager();
        assertThrows(IllegalStateException.class, () ->
            manager.update("missing", "Name", "email@example.com")
        );
    }
}

