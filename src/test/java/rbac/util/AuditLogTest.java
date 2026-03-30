package rbac.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AuditLogTest {

    @Test
    void logAndFilter() {
        AuditLog log = new AuditLog();
        log.log("X", "u1", "t1", "d1");
        log.log("Y", "u2", "t2", "d2");
        assertEquals(2, log.getAll().size());
        assertEquals(1, log.getByPerformer("u1").size());
        assertEquals(1, log.getByAction("Y").size());
    }
}
