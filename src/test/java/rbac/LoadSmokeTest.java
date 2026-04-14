package rbac;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rbac.filters.UserFilters;
import rbac.model.AssignmentMetadata;
import rbac.model.PermanentAssignment;
import rbac.model.Role;
import rbac.model.User;
import rbac.sort.UserSorters;

import static org.junit.jupiter.api.Assertions.*;

class LoadSmokeTest {

    @Test
    void concurrentOperationsDoNotCrashAndKeepBasicInvariants() throws Exception {
        RBACSystem sys = new RBACSystem();
        sys.initialize();

        int threads = Math.max(4, Runtime.getRuntime().availableProcessors());
        int ops = 150;

        // ensure at least one shared role exists
        Role r = new Role("LoadRole", "role for load test");
        synchronized (sys.getRoleManager()) {
            try { sys.getRoleManager().add(r); } catch (Exception ignored) {}
        }

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch done = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            int tid = t;
            pool.submit(() -> {
                try {
                    for (int i = 0; i < ops; i++) {
                        String username = "load_" + tid + "_" + i;
                        User u = User.create(username, "User " + username, username + "@example.com");

                        synchronized (sys.getUserManager()) {
                            try { sys.getUserManager().add(u); } catch (Exception ignored) {}
                            if (i % 7 == 0) {
                                try { sys.getUserManager().update(username, "Upd " + username, username + "@upd.example.com"); } catch (Exception ignored) {}
                            }
                            // read paths
                            sys.getUserManager().findAll(UserFilters.byEmailDomain("@example.com"), UserSorters.byUsername());
                        }

                        if (i % 5 == 0) {
                            synchronized (sys.getAssignmentManager()) {
                                try {
                                    sys.getAssignmentManager().add(new PermanentAssignment(
                                        u,
                                        r,
                                        AssignmentMetadata.now("system", "load")
                                    ));
                                } catch (Exception ignored) {}
                                sys.getAssignmentManager().getActiveAssignments();
                            }
                        }
                    }
                } finally {
                    done.countDown();
                }
            });
        }

        assertTrue(done.await(10, TimeUnit.SECONDS), "load workers did not finish");
        pool.shutdownNow();

        synchronized (sys.getUserManager()) {
            assertTrue(sys.getUserManager().count() >= 1, "expected users to exist");
            assertTrue(sys.getUserManager().findAll().stream().allMatch(x -> x != null && x.username() != null));
        }
        synchronized (sys.getAssignmentManager()) {
            assertTrue(sys.getAssignmentManager().findAll().stream().allMatch(x -> x != null && x.user() != null && x.role() != null));
        }
    }
}

