package com.rbac.manager;

import com.rbac.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {
    UserManager manager;

    @BeforeEach
    void setUp() {
        manager = new UserManager();
    }

    @Test
    void addAndFind() {
        User u = User.create("john", "John", "john@test.com");
        manager.add(u);
        assertEquals(1, manager.count());
        assertTrue(manager.findByUsername("john").isPresent());
    }

    @Test
    void duplicateThrows() {
        manager.add(User.create("a", "A", "a@t.com"));
        assertThrows(IllegalArgumentException.class, () -> manager.add(User.create("a", "B", "b@t.com")));
    }

    @Test
    void update() {
        manager.add(User.create("x", "X", "x@t.com"));
        manager.update("x", "X New", "xnew@t.com");
        User u = manager.findByUsername("x").get();
        assertEquals("X New", u.fullName());
        assertEquals("xnew@t.com", u.email());
    }
}
