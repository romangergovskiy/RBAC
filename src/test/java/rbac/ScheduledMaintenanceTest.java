package rbac;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import rbac.model.AssignmentMetadata;
import rbac.model.Role;
import rbac.model.TemporaryAssignment;
import rbac.model.User;

import static org.junit.jupiter.api.Assertions.*;

class ScheduledMaintenanceTest {

    @Test
    void maintenanceLogsStatsAndExpiredAssignments() throws Exception {
        RBACSystem sys = new RBACSystem();
        sys.initialize();

        // add an extra expired temp assignment
        User u = User.create("tempUser", "Temp User", "tempUser@example.com");
        sys.getUserManager().add(u);
        Role r = new Role("TempRole", "Temp role");
        sys.getRoleManager().add(r);
        sys.getAssignmentManager().add(new TemporaryAssignment(
            u,
            r,
            AssignmentMetadata.now("system", "test"),
            LocalDate.now().minusDays(2).toString(),
            false
        ));

        // speed up tick for test
        sys.startMaintenance(1);

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        boolean sawStats = false;
        boolean sawExpired = false;
        while (System.nanoTime() < deadline) {
            var all = sys.getAuditLog().getAll();
            sawStats = all.stream().anyMatch(e -> "STATS_TICK".equals(e.action()));
            sawExpired = all.stream().anyMatch(e -> "EXPIRED_ASSIGNMENTS".equals(e.action()));
            if (sawStats && sawExpired) break;
            Thread.sleep(50);
        }

        assertTrue(sawStats, "expected periodic stats tick in audit log");
        assertTrue(sawExpired, "expected expired assignments log entry");
    }
}

