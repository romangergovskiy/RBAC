package rbac;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RBACSystemTest {

    @Test
    void initializeCreatesDefaultData() {
        RBACSystem sys = new RBACSystem();
        sys.initialize();

        assertEquals(1, sys.getUserManager().count());
        assertTrue(sys.getUserManager().findByUsername("admin").isPresent());

        assertEquals(3, sys.getRoleManager().count());
        assertTrue(sys.getRoleManager().findByName("Admin").isPresent());
        assertTrue(sys.getRoleManager().findByName("Manager").isPresent());
        assertTrue(sys.getRoleManager().findByName("Viewer").isPresent());

        assertEquals(1, sys.getAssignmentManager().getActiveAssignments().size());
        assertEquals("admin", sys.getCurrentUser());
    }

    @Test
    void generateStatisticsFormatted() {
        RBACSystem sys = new RBACSystem();
        sys.initialize();

        String stats = sys.generateStatistics();
        assertTrue(stats.contains("Users: 1"));
        assertTrue(stats.contains("Roles: 3"));
        assertTrue(stats.contains("Assignments:"));
        assertTrue(stats.contains("Admin"));
    }
}
