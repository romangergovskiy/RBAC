package rbac.command;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import rbac.RBACSystem;
import rbac.filters.AssignmentFilter;
import rbac.filters.AssignmentFilters;
import rbac.filters.RoleFilter;
import rbac.filters.RoleFilters;
import rbac.filters.UserFilter;
import rbac.filters.UserFilters;
import rbac.manager.AssignmentManager;
import rbac.manager.RoleManager;
import rbac.manager.UserManager;
import rbac.model.AssignmentMetadata;
import rbac.model.Permission;
import rbac.model.PermanentAssignment;
import rbac.model.Role;
import rbac.model.RoleAssignment;
import rbac.model.TemporaryAssignment;
import rbac.model.User;
import rbac.sort.AssignmentSorters;
import rbac.sort.RoleSorters;
import rbac.sort.UserSorters;

public final class CommandRegistry {

    private CommandRegistry() {}

    public static void registerAll(CommandParser parser) {
        parser.registerCommand("help", "Show command list", (s, sys) -> parser.printHelp());

        parser.registerCommand("stats", "System statistics", (sc, sys) -> {
            System.out.println(sys.generateStatistics());
        });

        parser.registerCommand("clear", "Clear screen", (sc, sys) -> {
            for (int i = 0; i < 30; i++) System.out.println();
        });

        parser.registerCommand("exit", "Exit program", (sc, sys) -> {
            System.out.print("Save data? (y/n): ");
            sc.nextLine();
            System.out.print("Exit? (yes/no): ");
            if ("yes".equalsIgnoreCase(sc.nextLine().trim())) {
                System.out.println("Bye.");
                System.exit(0);
            }
        });

        parser.registerCommand("user-list", "List all users", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            List<User> users = um.findAll();
            users.sort(UserSorters.byUsername());
            printUserTable(users);
        });

        parser.registerCommand("user-create", "Create user", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            System.out.print("Username: ");
            String username = sc.nextLine().trim();
            System.out.print("Full name: ");
            String fullName = sc.nextLine().trim();
            System.out.print("Email: ");
            String email = sc.nextLine().trim();
            try {
                User u = User.create(username, fullName, email);
                um.add(u);
                System.out.println("User created: " + username);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("user-view", "View user details", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            AssignmentManager am = sys.getAssignmentManager();
            System.out.print("Username: ");
            String username = sc.nextLine().trim();
            Optional<User> ou = um.findByUsername(username);
            if (ou.isEmpty()) {
                System.out.println("User not found.");
                return;
            }
            User u = ou.get();
            System.out.println(u.format());
            List<RoleAssignment> assignments = am.findByUser(u);
            System.out.println("Roles:");
            for (var a : assignments) {
                if (a.isActive()) System.out.println("  - " + a.role().getName() + " (" + a.assignmentType() + ")");
            }
            Set<Permission> perms = am.getUserPermissions(u);
            System.out.println("Permissions:");
            for (var p : perms) System.out.println("  - " + p.format());
        });

        parser.registerCommand("user-update", "Update user", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            System.out.print("Username: ");
            String username = sc.nextLine().trim();
            System.out.print("New full name: ");
            String fullName = sc.nextLine().trim();
            System.out.print("New email: ");
            String email = sc.nextLine().trim();
            try {
                um.update(username, fullName, email);
                System.out.println("Updated.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("user-delete", "Delete user", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            AssignmentManager am = sys.getAssignmentManager();
            System.out.print("Username: ");
            String username = sc.nextLine().trim();
            Optional<User> ou = um.findByUsername(username);
            if (ou.isEmpty()) {
                System.out.println("User not found.");
                return;
            }
            System.out.print("Confirm deletion (да): ");
            if (!"да".equals(sc.nextLine().trim())) {
                System.out.println("Cancelled.");
                return;
            }
            User u = ou.get();
            for (var a : am.findByUser(u)) am.remove(a);
            um.remove(u);
            System.out.println("Deleted.");
        });

        parser.registerCommand("user-search", "Search users", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            System.out.println("1=username contains, 2=email contains, 3=email domain, 4=full name contains");
            System.out.print("Choice: ");
            int choice = parseInt(sc.nextLine(), 1);
            UserFilter filter = u -> true;
            if (choice == 1) { System.out.print("Substring: "); filter = UserFilters.byUsernameContains(sc.nextLine().trim()); }
            else if (choice == 2) { System.out.print("Substring: "); String sub = sc.nextLine().trim().toLowerCase(); filter = u -> u != null && u.email().toLowerCase().contains(sub); }
            else if (choice == 3) { System.out.print("Domain: "); filter = UserFilters.byEmailDomain(sc.nextLine().trim()); }
            else if (choice == 4) { System.out.print("Substring: "); filter = UserFilters.byFullNameContains(sc.nextLine().trim()); }
            List<User> list = um.findAll(filter, UserSorters.byUsername());
            printUserTable(list);
        });

        parser.registerCommand("role-list", "List roles", (sc, sys) -> {
            RoleManager rm = sys.getRoleManager();
            System.out.printf("%-20s %-10s %s%n", "Name", "Permissions", "ID");
            for (Role r : rm.findAll()) {
                System.out.printf("%-20s %-10d %s%n", r.getName(), r.getPermissions().size(), r.getId());
            }
        });

        parser.registerCommand("role-create", "Create role", (sc, sys) -> {
            RoleManager rm = sys.getRoleManager();
            System.out.print("Name: ");
            String name = sc.nextLine().trim();
            System.out.print("Description: ");
            String desc = sc.nextLine().trim();
            try {
                Role r = new Role(name, desc);
                rm.add(r);
                System.out.print("Add permissions? (y/n): ");
                String ans = sc.nextLine().trim();
                while ("y".equalsIgnoreCase(ans)) {
                    System.out.print("Permission name: ");
                    String pn = sc.nextLine().trim();
                    System.out.print("Resource: ");
                    String res = sc.nextLine().trim();
                    System.out.print("Description: ");
                    String pd = sc.nextLine().trim();
                    try {
                        rm.addPermissionToRole(name, new Permission(pn, res, pd));
                        System.out.println("Added.");
                    } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
                    System.out.print("Add more? (y/n): ");
                    ans = sc.nextLine().trim();
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("role-view", "View role", (sc, sys) -> {
            RoleManager rm = sys.getRoleManager();
            System.out.print("Role name: ");
            rm.findByName(sc.nextLine().trim()).ifPresentOrElse(r -> System.out.println(r.format()), () -> System.out.println("Not found."));
        });

        parser.registerCommand("role-update", "Update role", (sc, sys) -> {
            System.out.println("(Role name/description update not implemented - use role-add-permission)");
        });

        parser.registerCommand("role-delete", "Delete role", (sc, sys) -> {
            RoleManager rm = sys.getRoleManager();
            AssignmentManager am = sys.getAssignmentManager();
            System.out.print("Role name: ");
            String name = sc.nextLine().trim();
            Optional<Role> or = rm.findByName(name);
            if (or.isEmpty()) { System.out.println("Not found."); return; }
            Role r = or.get();
            var assigned = am.findByRole(r);
            if (!assigned.isEmpty()) {
                System.out.println("Role is assigned to: " + assigned.stream().map(a -> a.user().username()).collect(Collectors.joining(", ")));
            }
            System.out.print("Confirm deletion (да): ");
            if (!"да".equals(sc.nextLine().trim())) { System.out.println("Cancelled."); return; }
            for (var a : assigned) am.remove(a);
            rm.remove(r);
            System.out.println("Deleted.");
        });

        parser.registerCommand("role-add-permission", "Add permission to role", (sc, sys) -> {
            RoleManager rm = sys.getRoleManager();
            System.out.print("Role name: ");
            String rn = sc.nextLine().trim();
            System.out.print("Permission name: ");
            String pn = sc.nextLine().trim();
            System.out.print("Resource: ");
            String res = sc.nextLine().trim();
            System.out.print("Description: ");
            String pd = sc.nextLine().trim();
            try {
                rm.addPermissionToRole(rn, new Permission(pn, res, pd));
                System.out.println("Added.");
            } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
        });

        parser.registerCommand("role-remove-permission", "Remove permission from role", (sc, sys) -> {
            RoleManager rm = sys.getRoleManager();
            System.out.print("Role name: ");
            String rn = sc.nextLine().trim();
            Optional<Role> or = rm.findByName(rn);
            if (or.isEmpty()) { System.out.println("Not found."); return; }
            Role r = or.get();
            var perms = r.getPermissions().stream().toList();
            for (int i = 0; i < perms.size(); i++) System.out.println((i + 1) + ". " + perms.get(i).format());
            System.out.print("Number to remove: ");
            int n = parseInt(sc.nextLine(), 1);
            if (n >= 1 && n <= perms.size()) {
                rm.removePermissionFromRole(rn, perms.get(n - 1));
                System.out.println("Removed.");
            }
        });

        parser.registerCommand("role-search", "Search roles", (sc, sys) -> {
            RoleManager rm = sys.getRoleManager();
            System.out.println("1=name contains, 2=has permission, 3=min permission count");
            System.out.print("Choice: ");
            int c = parseInt(sc.nextLine(), 1);
            RoleFilter f = r -> true;
            if (c == 1) { System.out.print("Substring: "); f = RoleFilters.byNameContains(sc.nextLine().trim()); }
            else if (c == 2) { System.out.print("Permission name: "); String pn = sc.nextLine().trim(); System.out.print("Resource: "); String res = sc.nextLine().trim(); f = RoleFilters.hasPermission(pn, res); }
            else if (c == 3) { System.out.print("Min count: "); f = RoleFilters.hasAtLeastNPermissions(parseInt(sc.nextLine(), 0)); }
            List<Role> list = rm.findAll(f, RoleSorters.byName());
            for (Role r : list) System.out.println(r.getName() + " (" + r.getPermissions().size() + " perms)");
        });

        parser.registerCommand("assign-role", "Assign role to user", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            RoleManager rm = sys.getRoleManager();
            AssignmentManager am = sys.getAssignmentManager();
            System.out.print("Username: ");
            User u = um.findByUsername(sc.nextLine().trim()).orElse(null);
            if (u == null) { System.out.println("User not found."); return; }
            System.out.println("Available roles:");
            var roles = rm.findAll();
            for (int i = 0; i < roles.size(); i++) System.out.println((i + 1) + ". " + roles.get(i).getName());
            System.out.print("Choose (1-" + roles.size() + "): ");
            int idx = parseInt(sc.nextLine(), 1);
            if (idx < 1 || idx > roles.size()) { System.out.println("Invalid."); return; }
            Role role = roles.get(idx - 1);
            System.out.print("Type (permanent/temporary): ");
            String type = sc.nextLine().trim().toLowerCase();
            System.out.print("Reason: ");
            String reason = sc.nextLine().trim();
            String cur = sys.getCurrentUser() != null ? sys.getCurrentUser() : "admin";
            AssignmentMetadata meta = AssignmentMetadata.now(cur, reason);
            try {
                if ("temporary".startsWith(type) || "temp".equals(type)) {
                    System.out.print("Expires (YYYY-MM-DD): ");
                    String exp = sc.nextLine().trim();
                    am.add(new TemporaryAssignment(u, role, meta, exp, false));
                } else {
                    am.add(new PermanentAssignment(u, role, meta));
                }
                System.out.println("Assigned.");
            } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
        });

        parser.registerCommand("revoke-role", "Revoke role from user", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            AssignmentManager am = sys.getAssignmentManager();
            System.out.print("Username: ");
            User u = um.findByUsername(sc.nextLine().trim()).orElse(null);
            if (u == null) { System.out.println("User not found."); return; }
            var list = am.findByUser(u).stream().filter(RoleAssignment::isActive).toList();
            for (int i = 0; i < list.size(); i++) System.out.println((i + 1) + ". " + list.get(i).role().getName());
            System.out.print("Choose: ");
            int n = parseInt(sc.nextLine(), 1);
            if (n >= 1 && n <= list.size()) {
                am.revokeAssignment(list.get(n - 1).assignmentId());
                System.out.println("Revoked.");
            }
        });

        parser.registerCommand("assignment-list", "List all assignments", (sc, sys) -> {
            AssignmentManager am = sys.getAssignmentManager();
            printAssignmentTable(am.findAll().stream().sorted(AssignmentSorters.byUsername()).toList());
        });

        parser.registerCommand("assignment-list-user", "List user assignments", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            AssignmentManager am = sys.getAssignmentManager();
            System.out.print("Username: ");
            User u = um.findByUsername(sc.nextLine().trim()).orElse(null);
            if (u == null) { System.out.println("Not found."); return; }
            var list = am.findByUser(u);
            for (var a : list) System.out.println("[" + a.assignmentType() + "] " + a.role().getName() + " -> " + a.user().username() + ", " + (a.isActive() ? "ACTIVE" : "INACTIVE"));
        });

        parser.registerCommand("assignment-list-role", "List users with role", (sc, sys) -> {
            RoleManager rm = sys.getRoleManager();
            AssignmentManager am = sys.getAssignmentManager();
            System.out.print("Role name: ");
            Role r = rm.findByName(sc.nextLine().trim()).orElse(null);
            if (r == null) { System.out.println("Not found."); return; }
            for (var a : am.findByRole(r)) if (a.isActive()) System.out.println(a.user().username());
        });

        parser.registerCommand("assignment-active", "Active assignments", (sc, sys) -> {
            printAssignmentTable(sys.getAssignmentManager().getActiveAssignments().stream().sorted(AssignmentSorters.byUsername()).toList());
        });

        parser.registerCommand("assignment-expired", "Expired assignments", (sc, sys) -> {
            printAssignmentTable(sys.getAssignmentManager().getExpiredAssignments());
        });

        parser.registerCommand("assignment-extend", "Extend temporary assignment", (sc, sys) -> {
            AssignmentManager am = sys.getAssignmentManager();
            System.out.print("Assignment ID: ");
            String id = sc.nextLine().trim();
            System.out.print("New expiration (YYYY-MM-DD): ");
            String exp = sc.nextLine().trim();
            try {
                am.extendTemporaryAssignment(id, exp);
                System.out.println("Extended.");
            } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
        });

        parser.registerCommand("assignment-search", "Search assignments", (sc, sys) -> {
            AssignmentManager am = sys.getAssignmentManager();
            System.out.println("1=by user, 2=by role, 3=by type, 4=active only, 5=inactive, 6=assigned after, 7=expiring before");
            System.out.print("Choice: ");
            int c = parseInt(sc.nextLine(), 1);
            AssignmentFilter f = a -> true;
            if (c == 1) { System.out.print("Username: "); User u = sys.getUserManager().findByUsername(sc.nextLine().trim()).orElse(null); if (u != null) f = AssignmentFilters.byUser(u); }
            else if (c == 2) { System.out.print("Role: "); Role r = sys.getRoleManager().findByName(sc.nextLine().trim()).orElse(null); if (r != null) f = AssignmentFilters.byRole(r); }
            else if (c == 3) { System.out.print("PERMANENT or TEMPORARY: "); f = AssignmentFilters.byType(sc.nextLine().trim()); }
            else if (c == 4) f = AssignmentFilters.activeOnly();
            else if (c == 5) f = AssignmentFilters.inactiveOnly();
            else if (c == 6) { System.out.print("Date: "); f = AssignmentFilters.assignedAfter(sc.nextLine().trim()); }
            else if (c == 7) { System.out.print("Date: "); f = AssignmentFilters.expiringBefore(sc.nextLine().trim()); }
            printAssignmentTable(am.findAll(f, AssignmentSorters.byUsername()));
        });

        parser.registerCommand("permissions-user", "User permissions", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            AssignmentManager am = sys.getAssignmentManager();
            System.out.print("Username: ");
            User u = um.findByUsername(sc.nextLine().trim()).orElse(null);
            if (u == null) { System.out.println("Not found."); return; }
            var perms = am.getUserPermissions(u);
            var byResource = perms.stream().collect(Collectors.groupingBy(Permission::resource));
            for (var e : byResource.entrySet()) {
                System.out.println(e.getKey() + ": " + e.getValue().stream().map(Permission::name).collect(Collectors.joining(", ")));
            }
        });

        parser.registerCommand("permissions-check", "Check user permission", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            AssignmentManager am = sys.getAssignmentManager();
            System.out.print("Username: ");
            User u = um.findByUsername(sc.nextLine().trim()).orElse(null);
            if (u == null) { System.out.println("Not found."); return; }
            System.out.print("Permission name: ");
            String pn = sc.nextLine().trim();
            System.out.print("Resource: ");
            String res = sc.nextLine().trim();
            boolean has = am.userHasPermission(u, pn, res);
            System.out.println(has ? "Yes, user has this permission" : "No");
        });

        parser.registerCommand("save", "Save data to file", (sc, sys) -> {
            System.out.print("File path: ");
            String path = sc.nextLine().trim();
            try {
                DataStorage.save(sys, path);
                System.out.println("Saved.");
            } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
        });

        parser.registerCommand("load", "Load data from file", (sc, sys) -> {
            System.out.print("File path: ");
            String path = sc.nextLine().trim();
            try {
                DataStorage.load(sys, path);
                System.out.println("Loaded.");
            } catch (Exception e) { System.out.println("Error: " + e.getMessage()); }
        });
    }

    private static void printUserTable(List<User> users) {
        System.out.printf("%-20s %-25s %s%n", "Username", "Full Name", "Email");
        for (User u : users) System.out.printf("%-20s %-25s %s%n", u.username(), u.fullName(), u.email());
    }

    private static void printAssignmentTable(List<RoleAssignment> list) {
        System.out.printf("%-15s %-15s %-10s %-8s %s%n", "Username", "Role", "Type", "Status", "Assigned At");
        for (var a : list) {
            String status = a.isActive() ? "ACTIVE" : "INACTIVE";
            System.out.printf("%-15s %-15s %-10s %-8s %s%n", a.user().username(), a.role().getName(), a.assignmentType(), status, a.metadata().assignedAt());
        }
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }
}
