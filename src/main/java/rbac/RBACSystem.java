package rbac;

import rbac.manager.AssignmentManager;
import rbac.manager.RoleManager;
import rbac.manager.UserManager;
import rbac.util.AuditLog;
import rbac.util.DateUtils;

import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class RBACSystem {
    private final UserManager userManager = new UserManager();
    private final RoleManager roleManager = new RoleManager();
    private final AssignmentManager assignmentManager = new AssignmentManager();
    private final AuditLog auditLog = new AuditLog();
    private final ScheduledExecutorService scheduler;
    private String currentUser;

    public RBACSystem() {
        ThreadFactory tf = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, "rbac-scheduler");
                t.setDaemon(true);
                return t;
            }
        };
        this.scheduler = Executors.newSingleThreadScheduledExecutor(tf);
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public AuditLog getAuditLog() {
        return auditLog;
    }

    public void log(String action, String target, String details) {
        String performer = currentUser != null ? currentUser : "system";
        auditLog.log(action, performer, target, details);
    }

    public void startMaintenance(int intervalSeconds) {
        int n = Math.max(1, intervalSeconds);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                var expired = assignmentManager.getExpiredAssignments();
                if (!expired.isEmpty()) {
                    String yesterday = LocalDate.now().minusDays(1).toString();
                    for (var a : expired) {
                        if (a instanceof rbac.model.TemporaryAssignment ta) {
                            ta.extend(yesterday);
                        }
                    }
                    log("EXPIRED_ASSIGNMENTS", "-", "expired=" + expired.size() + ", date=" + DateUtils.getCurrentDate());
                }

                String stats = generateStatistics().replace("\n", " | ").trim();
                log("STATS_TICK", "-", stats);
            } catch (Exception ignored) {
            }
        }, n, n, TimeUnit.SECONDS);
    }

    public void initialize() {
        var pReadUsers = new rbac.model.Permission("READ", "users", "View user list");
        var pWriteUsers = new rbac.model.Permission("WRITE", "users", "Create and edit users");
        var pDeleteUsers = new rbac.model.Permission("DELETE", "users", "Delete users");
        var pReadReports = new rbac.model.Permission("READ", "reports", "View reports");
        var pWriteReports = new rbac.model.Permission("WRITE", "reports", "Create reports");
        var pReadSettings = new rbac.model.Permission("READ", "settings", "View settings");
        var pWriteSettings = new rbac.model.Permission("WRITE", "settings", "Edit settings");

        var adminRole = new rbac.model.Role("Admin", "Full system access");
        adminRole.addPermission(pReadUsers);
        adminRole.addPermission(pWriteUsers);
        adminRole.addPermission(pDeleteUsers);
        adminRole.addPermission(pReadReports);
        adminRole.addPermission(pWriteReports);
        adminRole.addPermission(pReadSettings);
        adminRole.addPermission(pWriteSettings);

        var managerRole = new rbac.model.Role("Manager", "Manage users and reports");
        managerRole.addPermission(pReadUsers);
        managerRole.addPermission(pWriteUsers);
        managerRole.addPermission(pReadReports);
        managerRole.addPermission(pWriteReports);

        var viewerRole = new rbac.model.Role("Viewer", "Read-only access");
        viewerRole.addPermission(pReadUsers);
        viewerRole.addPermission(pReadReports);
        viewerRole.addPermission(pReadSettings);

        roleManager.add(adminRole);
        roleManager.add(managerRole);
        roleManager.add(viewerRole);

        var adminUser = rbac.model.User.create("admin", "System Administrator", "admin@system.local");
        userManager.add(adminUser);

        var meta = rbac.model.AssignmentMetadata.now("system", "Initial setup");
        var assignment = new rbac.model.PermanentAssignment(adminUser, adminRole, meta);
        assignmentManager.add(assignment);

        currentUser = "admin";

        // default maintenance tick
        startMaintenance(5);
    }

    public String generateStatistics() {
        int users = userManager.count();
        int roles = roleManager.count();
        int total = assignmentManager.count();
        int active = assignmentManager.getActiveAssignments().size();
        int expired = assignmentManager.getExpiredAssignments().size();
        double avgRoles = users > 0 ? (double) active / users : 0;

        var roleCounts = new java.util.HashMap<String, Integer>();
        for (var a : assignmentManager.getActiveAssignments()) {
            String name = a.role().getName();
            roleCounts.merge(name, 1, Integer::sum);
        }
        var topRoles = roleCounts.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(3)
            .toList();

        var sb = new StringBuilder();
        sb.append("=== RBAC Statistics ===\n");
        sb.append("Users: ").append(users).append("\n");
        sb.append("Roles: ").append(roles).append("\n");
        sb.append("Assignments: ").append(total).append(" (active: ").append(active).append(", expired: ").append(expired).append(")\n");
        sb.append("Avg roles per user: ").append(String.format("%.1f", avgRoles)).append("\n");
        sb.append("Top 3 roles: ");
        if (topRoles.isEmpty()) sb.append("none");
        else for (int i = 0; i < topRoles.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(topRoles.get(i).getKey()).append(" (").append(topRoles.get(i).getValue()).append(")");
        }
        sb.append("\n");
        return sb.toString();
    }
}
