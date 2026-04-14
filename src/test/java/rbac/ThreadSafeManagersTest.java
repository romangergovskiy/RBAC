package rbac;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import rbac.manager.AssignmentManager;
import rbac.manager.RoleManager;
import rbac.manager.UserManager;
import rbac.model.AssignmentMetadata;
import rbac.model.PermanentAssignment;
import rbac.model.Permission;
import rbac.model.Role;
import rbac.model.User;

import static org.junit.jupiter.api.Assertions.*;

class ThreadSafeManagersTest {

    @Test
    void concurrentUserRoleAndAssignmentOperationsDoNotCorruptState() throws Exception {
        UserManager users = new UserManager();
        RoleManager roles = new RoleManager();
        AssignmentManager assignments = new AssignmentManager();

        int threads = Math.max(4, Runtime.getRuntime().availableProcessors());
        int opsPerThread = 200;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger failures = new AtomicInteger(0);

        Permission readUsers = new Permission("READ", "users", "read users");
        Permission writeUsers = new Permission("WRITE", "users", "write users");

        for (int t = 0; t < threads; t++) {
            int threadId = t;
            pool.submit(() -> {
                try {
                    start.await(2, TimeUnit.SECONDS);
                    for (int i = 0; i < opsPerThread; i++) {
                        String uname = "u" + threadId + "_" + i;
                        try {
                            users.add(User.create(uname, "User " + uname, uname + "@example.com"));
                        } catch (RuntimeException ignored) {
                            // allowed: duplicates are prevented, but other threads may race on same ids in future changes
                        }

                        if (i % 5 == 0) {
                            String target = "u" + threadId + "_" + Math.max(0, i - 1);
                            try {
                                users.update(target, "Updated " + target, target + "@updated.example.com");
                            } catch (RuntimeException ignored) {
                                // allowed: might not exist yet
                            }
                        }

                        String roleName = (i % 2 == 0) ? "Admin" : "Viewer";
                        try {
                            roles.add(new Role(roleName, roleName + " role"));
                        } catch (RuntimeException ignored) {
                            // allowed: role already exists
                        }

                        try {
                            roles.addPermissionToRole("Admin", readUsers);
                            roles.addPermissionToRole("Admin", writeUsers);
                            roles.addPermissionToRole("Viewer", readUsers);
                        } catch (RuntimeException ignored) {
                            // allowed: role may not exist yet
                        }

                        users.findById(uname);
                        roles.findByName(roleName);

                        if (i % 4 == 0) {
                            User u = users.findById(uname).orElse(null);
                            Role r = roles.findByName(roleName).orElse(null);
                            if (u != null && r != null) {
                                try {
                                    assignments.add(new PermanentAssignment(u, r, AssignmentMetadata.now("system", "load")));
                                } catch (RuntimeException ignored) {
                                    // allowed: duplicate active assignment prevention or duplicate id
                                }
                            }
                        }

                        if (i % 10 == 0) {
                            List<User> snapshot = users.findAll();
                            if (!snapshot.isEmpty()) {
                                User any = snapshot.get(snapshot.size() / 2);
                                assignments.getUserPermissions(any);
                            }
                        }
                    }
                } catch (Throwable e) {
                    failures.incrementAndGet();
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS), "workers did not finish in time");
        pool.shutdownNow();

        assertEquals(0, failures.get(), "unexpected exceptions in concurrent execution");

        // basic invariants: no nulls, permissions set is stable to iterate
        for (Role r : roles.findAll()) {
            assertNotNull(r.getId());
            Set<Permission> perms = r.getPermissions();
            assertNotNull(perms);
        }
    }
}

