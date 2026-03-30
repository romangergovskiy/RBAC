package rbac.command;

import java.util.ArrayList;
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
import rbac.util.ConsoleUtils;
import rbac.util.FormatUtils;
import rbac.util.ReportGenerator;
import rbac.util.ValidationUtils;

public final class CommandRegistry {

    private CommandRegistry() {}

    public static void registerAll(CommandParser parser) {
        parser.registerCommand("help", "Show command list", (s, sys) -> parser.printHelp());

        parser.registerCommand("audit-log", "View audit log", (sc, sys) -> sys.getAuditLog().printLog());

        parser.registerCommand("stats", "System statistics", (sc, sys) -> {
            System.out.println(FormatUtils.formatBox(sys.generateStatistics()));
        });

        parser.registerCommand("clear", "Clear screen", (sc, sys) -> {
            for (int i = 0; i < 30; i++) System.out.println();
        });

        parser.registerCommand("exit", "Exit program", (sc, sys) -> {
            ConsoleUtils.promptYesNo(sc, "Save data before exit?");
            if (ConsoleUtils.promptYesNo(sc, "Exit?")) {
                System.out.println("Bye.");
                System.exit(0);
            }
        });

        parser.registerCommand("report-users", "User report", (sc, sys) -> {
            String r = ReportGenerator.generateUserReport(sys.getUserManager(), sys.getAssignmentManager());
            System.out.println(r);
            if (ConsoleUtils.promptYesNo(sc, "Save report to file?")) {
                String path = ConsoleUtils.promptString(sc, "File path: ", true);
                try {
                    ReportGenerator.exportToFile(r, path);
                    System.out.println("Saved.");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        });

        parser.registerCommand("report-roles", "Role report", (sc, sys) -> {
            String r = ReportGenerator.generateRoleReport(sys.getRoleManager(), sys.getAssignmentManager());
            System.out.println(r);
            if (ConsoleUtils.promptYesNo(sc, "Save report to file?")) {
                String path = ConsoleUtils.promptString(sc, "File path: ", true);
                try {
                    ReportGenerator.exportToFile(r, path);
                    System.out.println("Saved.");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        });

        parser.registerCommand("report-matrix", "Permission matrix", (sc, sys) -> {
            String r = ReportGenerator.generatePermissionMatrix(sys.getUserManager(), sys.getAssignmentManager());
            System.out.println(r);
            if (ConsoleUtils.promptYesNo(sc, "Save report to file?")) {
                String path = ConsoleUtils.promptString(sc, "File path: ", true);
                try {
                    ReportGenerator.exportToFile(r, path);
                    System.out.println("Saved.");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        });

        parser.registerCommand("user-list", "List all users", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            List<User> users = new ArrayList<>(um.findAll());
            users.sort(UserSorters.byUsername());
            printUserTable(users);
        });

        parser.registerCommand("user-create", "Create user", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            String username = ConsoleUtils.promptString(sc, "Username: ", true);
            String fullName = ConsoleUtils.promptString(sc, "Full name: ", true);
            String email = ConsoleUtils.promptString(sc, "Email: ", true);
            try {
                User u = User.create(username, fullName, email);
                um.add(u);
                sys.log("USER_CREATE", u.username(), "created");
                System.out.println("User created: " + username);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("user-view", "View user details", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            AssignmentManager am = sys.getAssignmentManager();
            String username = ConsoleUtils.promptString(sc, "Username: ", true);
            Optional<User> ou = um.findByUsername(username);
            if (ou.isEmpty()) {
                System.out.println("User not found.");
                return;
            }
            User u = ou.get();
            System.out.println(FormatUtils.formatBox(u.format()));
            List<RoleAssignment> assignments = am.findByUser(u);
            System.out.println(FormatUtils.formatHeader("Roles"));
            for (var a : assignments) {
                if (a.isActive()) System.out.println("  - " + a.role().getName() + " (" + a.assignmentType() + ")");
            }
            Set<Permission> perms = am.getUserPermissions(u);
            System.out.println(FormatUtils.formatHeader("Permissions"));
            for (var p : perms) System.out.println("  - " + p.format());
        });

        parser.registerCommand("user-update", "Update user", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            String username = ConsoleUtils.promptString(sc, "Username: ", true);
            String fullName = ConsoleUtils.promptString(sc, "New full name: ", true);
            String email = ConsoleUtils.promptString(sc, "New email: ", true);
            try {
                um.update(username, fullName, email);
                sys.log("USER_UPDATE", username, "updated");
                System.out.println("Updated.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("user-delete", "Delete user", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            AssignmentManager am = sys.getAssignmentManager();
            String username = ConsoleUtils.promptString(sc, "Username: ", true);
            Optional<User> ou = um.findByUsername(username);
            if (ou.isEmpty()) {
                System.out.println("User not found.");
                return;
            }
            if (!ConsoleUtils.promptString(sc, "Confirm deletion (да): ", false).equals("да")) {
                System.out.println("Cancelled.");
                return;
            }
            User u = ou.get();
            for (var a : am.findByUser(u)) am.remove(a);
            um.remove(u);
            sys.log("USER_DELETE", username, "deleted");
            System.out.println("Deleted.");
        });

        parser.registerCommand("user-search", "Search users", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            System.out.println("1=username contains, 2=email contains, 3=email domain, 4=full name contains");
            int choice = ConsoleUtils.promptInt(sc, "Choice: ", 1, 4);
            UserFilter filter = x -> true;
            if (choice == 1) {
                String sub = ConsoleUtils.promptString(sc, "Substring: ", true);
                filter = UserFilters.byUsernameContains(sub);
            } else if (choice == 2) {
                String sub = ConsoleUtils.promptString(sc, "Substring: ", true).toLowerCase();
                filter = u -> u != null && u.email().toLowerCase().contains(sub);
            } else if (choice == 3) {
                String dom = ConsoleUtils.promptString(sc, "Domain: ", true);
                filter = UserFilters.byEmailDomain(dom);
            } else if (choice == 4) {
                String sub = ConsoleUtils.promptString(sc, "Substring: ", true);
                filter = UserFilters.byFullNameContains(sub);
            }
            List<User> list = um.findAll(filter, UserSorters.byUsername());
            printUserTable(list);
        });

        parser.registerCommand("role-list", "List roles", (sc, sys) -> {
            RoleManager rm = sys.getRoleManager();
            List<String[]> rows = new ArrayList<>();
            for (Role r : rm.findAll()) {
                rows.add(new String[] { r.getName(), String.valueOf(r.getPermissions().size()), r.getId() });
            }
            System.out.print(FormatUtils.formatTable(new String[] { "Name", "Permissions", "ID" }, rows));
        });

        parser.registerCommand("role-create", "Create role", (sc, sys) -> {
            RoleManager rm = sys.getRoleManager();
            String name = ConsoleUtils.promptString(sc, "Name: ", true);
            String desc = ConsoleUtils.promptString(sc, "Description: ", true);
            try {
                Role r = new Role(name, desc);
                rm.add(r);
                sys.log("ROLE_CREATE", name, "created");
                if (ConsoleUtils.promptYesNo(sc, "Add permissions?")) {
                    String ans = "y";
                    while ("y".equalsIgnoreCase(ans)) {
                        String pn = ConsoleUtils.promptString(sc, "Permission name: ", true);
                        String res = ConsoleUtils.promptString(sc, "Resource: ", true);
                        String pd = ConsoleUtils.promptString(sc, "Description: ", true);
                        try {
                            rm.addPermissionToRole(name, new Permission(pn, res, pd));
                            System.out.println("Added.");
                        } catch (Exception e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                        ans = ConsoleUtils.promptYesNo(sc, "Add more?") ? "y" : "n";
                    }
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("role-view", "View role", (sc, sys) -> {
            RoleManager rm = sys.getRoleManager();
            String name = ConsoleUtils.promptString(sc, "Role name: ", true);
            rm.findByName(name).ifPresentOrElse(r -> System.out.println(FormatUtils.formatBox(r.format())), () -> System.out.println("Not found."));
        });

        parser.registerCommand("role-update", "Update role", (sc, sys) -> {
            System.out.println("(Use role-add-permission / role-delete flow; name change via new role.)");
        });

        parser.registerCommand("role-delete", "Delete role", (sc, sys) -> {
            RoleManager rm = sys.getRoleManager();
            AssignmentManager am = sys.getAssignmentManager();
            String name = ConsoleUtils.promptString(sc, "Role name: ", true);
            Optional<Role> or = rm.findByName(name);
            if (or.isEmpty()) {
                System.out.println("Not found.");
                return;
            }
            Role r = or.get();
            var assigned = am.findByRole(r);
            if (!assigned.isEmpty()) {
                System.out.println("Role is assigned to: " + assigned.stream().map(a -> a.user().username()).collect(Collectors.joining(", ")));
            }
            if (!ConsoleUtils.promptString(sc, "Confirm deletion (да): ", false).equals("да")) {
                System.out.println("Cancelled.");
                return;
            }
            for (var a : assigned) am.remove(a);
            rm.remove(r);
            sys.log("ROLE_DELETE", name, "deleted");
            System.out.println("Deleted.");
        });

        parser.registerCommand("role-add-permission", "Add permission to role", (sc, sys) -> {
            RoleManager rm = sys.getRoleManager();
            String rn = ConsoleUtils.promptString(sc, "Role name: ", true);
            String pn = ConsoleUtils.promptString(sc, "Permission name: ", true);
            String res = ConsoleUtils.promptString(sc, "Resource: ", true);
            String pd = ConsoleUtils.promptString(sc, "Description: ", true);
            try {
                rm.addPermissionToRole(rn, new Permission(pn, res, pd));
                sys.log("ROLE_PERM_ADD", rn, pn + " on " + res);
                System.out.println("Added.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("role-remove-permission", "Remove permission from role", (sc, sys) -> {
            RoleManager rm = sys.getRoleManager();
            String rn = ConsoleUtils.promptString(sc, "Role name: ", true);
            Optional<Role> or = rm.findByName(rn);
            if (or.isEmpty()) {
                System.out.println("Not found.");
                return;
            }
            Role r = or.get();
            var perms = r.getPermissions().stream().toList();
            if (perms.isEmpty()) {
                System.out.println("No permissions.");
                return;
            }
            for (int i = 0; i < perms.size(); i++) System.out.println((i + 1) + ". " + perms.get(i).format());
            int idx = ConsoleUtils.promptInt(sc, "Number to remove (1-" + perms.size() + "): ", 1, perms.size());
            rm.removePermissionFromRole(rn, perms.get(idx - 1));
            sys.log("ROLE_PERM_REMOVE", rn, "removed #" + idx);
            System.out.println("Removed.");
        });

        parser.registerCommand("role-search", "Search roles", (sc, sys) -> {
            RoleManager rm = sys.getRoleManager();
            System.out.println("1=name contains, 2=has permission, 3=min permission count");
            int c = ConsoleUtils.promptInt(sc, "Choice: ", 1, 3);
            RoleFilter f = x -> true;
            if (c == 1) {
                String sub = ConsoleUtils.promptString(sc, "Substring: ", true);
                f = RoleFilters.byNameContains(sub);
            } else if (c == 2) {
                String pn = ConsoleUtils.promptString(sc, "Permission name: ", true);
                String res = ConsoleUtils.promptString(sc, "Resource: ", true);
                f = RoleFilters.hasPermission(pn, res);
            } else if (c == 3) {
                int min = ConsoleUtils.promptInt(sc, "Min count: ", 0, 1000);
                f = RoleFilters.hasAtLeastNPermissions(min);
            }
            List<Role> list = rm.findAll(f, RoleSorters.byName());
            List<String[]> rows = new ArrayList<>();
            for (Role role : list) rows.add(new String[] { role.getName(), String.valueOf(role.getPermissions().size()) });
            System.out.print(FormatUtils.formatTable(new String[] { "Name", "Perms" }, rows));
        });

        parser.registerCommand("assign-role", "Assign role to user", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            RoleManager rm = sys.getRoleManager();
            AssignmentManager am = sys.getAssignmentManager();
            String un = ConsoleUtils.promptString(sc, "Username: ", true);
            User u = um.findByUsername(un).orElse(null);
            if (u == null) {
                System.out.println("User not found.");
                return;
            }
            var roles = rm.findAll();
            if (roles.isEmpty()) {
                System.out.println("No roles.");
                return;
            }
            for (int i = 0; i < roles.size(); i++) System.out.println((i + 1) + ". " + roles.get(i).getName());
            int ri = ConsoleUtils.promptInt(sc, "Choose role (1-" + roles.size() + "): ", 1, roles.size());
            Role role = roles.get(ri - 1);
            String type = ConsoleUtils.promptString(sc, "Type (permanent/temporary): ", true).toLowerCase();
            String reason = ConsoleUtils.promptString(sc, "Reason: ", true);
            String cur = sys.getCurrentUser() != null ? sys.getCurrentUser() : "admin";
            AssignmentMetadata meta = AssignmentMetadata.now(cur, reason);
            try {
                if (type.startsWith("temp")) {
                    String exp = ConsoleUtils.promptString(sc, "Expires (YYYY-MM-DD): ", true);
                    if (!ValidationUtils.isValidDate(exp)) {
                        System.out.println("Invalid date.");
                        return;
                    }
                    am.add(new TemporaryAssignment(u, role, meta, exp, false));
                } else {
                    am.add(new PermanentAssignment(u, role, meta));
                }
                sys.log("ASSIGN", u.username(), role.getName());
                System.out.println("Assigned.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("revoke-role", "Revoke role from user", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            AssignmentManager am = sys.getAssignmentManager();
            String un = ConsoleUtils.promptString(sc, "Username: ", true);
            User u = um.findByUsername(un).orElse(null);
            if (u == null) {
                System.out.println("User not found.");
                return;
            }
            var list = am.findByUser(u).stream().filter(RoleAssignment::isActive).toList();
            if (list.isEmpty()) {
                System.out.println("No active assignments.");
                return;
            }
            for (int i = 0; i < list.size(); i++) System.out.println((i + 1) + ". " + list.get(i).role().getName());
            int n = ConsoleUtils.promptInt(sc, "Choose (1-" + list.size() + "): ", 1, list.size());
            RoleAssignment chosen = list.get(n - 1);
            am.revokeAssignment(chosen.assignmentId());
            sys.log("REVOKE", u.username(), chosen.role().getName());
            System.out.println("Revoked.");
        });

        parser.registerCommand("assignment-list", "List all assignments", (sc, sys) -> {
            AssignmentManager am = sys.getAssignmentManager();
            printAssignmentTable(am.findAll().stream().sorted(AssignmentSorters.byUsername()).toList());
        });

        parser.registerCommand("assignment-list-user", "List user assignments", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            AssignmentManager am = sys.getAssignmentManager();
            String un = ConsoleUtils.promptString(sc, "Username: ", true);
            User u = um.findByUsername(un).orElse(null);
            if (u == null) {
                System.out.println("Not found.");
                return;
            }
            var list = am.findByUser(u);
            for (var a : list) {
                System.out.println("[" + a.assignmentType() + "] " + a.role().getName() + " -> " + a.user().username() + ", " + (a.isActive() ? "ACTIVE" : "INACTIVE"));
            }
        });

        parser.registerCommand("assignment-list-role", "List users with role", (sc, sys) -> {
            RoleManager rm = sys.getRoleManager();
            AssignmentManager am = sys.getAssignmentManager();
            String name = ConsoleUtils.promptString(sc, "Role name: ", true);
            Role r = rm.findByName(name).orElse(null);
            if (r == null) {
                System.out.println("Not found.");
                return;
            }
            for (var a : am.findByRole(r))
                if (a.isActive()) System.out.println(a.user().username());
        });

        parser.registerCommand("assignment-active", "Active assignments", (sc, sys) -> {
            printAssignmentTable(sys.getAssignmentManager().getActiveAssignments().stream().sorted(AssignmentSorters.byUsername()).toList());
        });

        parser.registerCommand("assignment-expired", "Expired assignments", (sc, sys) -> {
            printAssignmentTable(sys.getAssignmentManager().getExpiredAssignments());
        });

        parser.registerCommand("assignment-extend", "Extend temporary assignment", (sc, sys) -> {
            AssignmentManager am = sys.getAssignmentManager();
            String id = ConsoleUtils.promptString(sc, "Assignment ID: ", true);
            String exp = ConsoleUtils.promptString(sc, "New expiration (YYYY-MM-DD): ", true);
            if (!ValidationUtils.isValidDate(exp)) {
                System.out.println("Invalid date.");
                return;
            }
            try {
                am.extendTemporaryAssignment(id, exp);
                sys.log("ASSIGN_EXTEND", id, exp);
                System.out.println("Extended.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("assignment-search", "Search assignments", (sc, sys) -> {
            AssignmentManager am = sys.getAssignmentManager();
            System.out.println("1=by user, 2=by role, 3=by type, 4=active only, 5=inactive, 6=assigned after, 7=expiring before");
            int c = ConsoleUtils.promptInt(sc, "Choice: ", 1, 7);
            AssignmentFilter f = a -> true;
            if (c == 1) {
                String username = ConsoleUtils.promptString(sc, "Username: ", true);
                User u = sys.getUserManager().findByUsername(username).orElse(null);
                if (u != null) f = AssignmentFilters.byUser(u);
            } else if (c == 2) {
                String rn = ConsoleUtils.promptString(sc, "Role: ", true);
                Role r = sys.getRoleManager().findByName(rn).orElse(null);
                if (r != null) f = AssignmentFilters.byRole(r);
            } else if (c == 3) {
                String t = ConsoleUtils.promptString(sc, "PERMANENT or TEMPORARY: ", true);
                f = AssignmentFilters.byType(t);
            } else if (c == 4) f = AssignmentFilters.activeOnly();
            else if (c == 5) f = AssignmentFilters.inactiveOnly();
            else if (c == 6) {
                String d = ConsoleUtils.promptString(sc, "Date: ", true);
                f = AssignmentFilters.assignedAfter(d);
            } else if (c == 7) {
                String d = ConsoleUtils.promptString(sc, "Date: ", true);
                f = AssignmentFilters.expiringBefore(d);
            }
            printAssignmentTable(am.findAll(f, AssignmentSorters.byUsername()));
        });

        parser.registerCommand("permissions-user", "User permissions", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            AssignmentManager am = sys.getAssignmentManager();
            String un = ConsoleUtils.promptString(sc, "Username: ", true);
            User u = um.findByUsername(un).orElse(null);
            if (u == null) {
                System.out.println("Not found.");
                return;
            }
            var perms = am.getUserPermissions(u);
            var byResource = perms.stream().collect(Collectors.groupingBy(Permission::resource));
            for (var e : byResource.entrySet()) {
                System.out.println(e.getKey() + ": " + e.getValue().stream().map(Permission::name).collect(Collectors.joining(", ")));
            }
        });

        parser.registerCommand("permissions-check", "Check user permission", (sc, sys) -> {
            UserManager um = sys.getUserManager();
            AssignmentManager am = sys.getAssignmentManager();
            String un = ConsoleUtils.promptString(sc, "Username: ", true);
            User u = um.findByUsername(un).orElse(null);
            if (u == null) {
                System.out.println("Not found.");
                return;
            }
            String pn = ConsoleUtils.promptString(sc, "Permission name: ", true);
            String res = ConsoleUtils.promptString(sc, "Resource: ", true);
            boolean has = am.userHasPermission(u, pn, res);
            System.out.println(has ? "Yes, user has this permission" : "No");
        });

        parser.registerCommand("save", "Save data to file", (sc, sys) -> {
            String path = ConsoleUtils.promptString(sc, "File path: ", true);
            try {
                DataStorage.save(sys, path);
                sys.log("SAVE", path, "data saved");
                System.out.println("Saved.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });

        parser.registerCommand("load", "Load data from file", (sc, sys) -> {
            String path = ConsoleUtils.promptString(sc, "File path: ", true);
            try {
                DataStorage.load(sys, path);
                sys.log("LOAD", path, "data loaded");
                System.out.println("Loaded.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
    }

    private static void printUserTable(List<User> users) {
        List<String[]> rows = new ArrayList<>();
        for (User u : users) rows.add(new String[] { u.username(), u.fullName(), u.email() });
        System.out.print(FormatUtils.formatTable(new String[] { "Username", "Full Name", "Email" }, rows));
    }

    private static void printAssignmentTable(List<RoleAssignment> list) {
        List<String[]> rows = new ArrayList<>();
        for (var a : list) {
            String status = a.isActive() ? "ACTIVE" : "INACTIVE";
            rows.add(new String[] { a.user().username(), a.role().getName(), a.assignmentType(), status, a.metadata().assignedAt() });
        }
        System.out.print(FormatUtils.formatTable(new String[] { "Username", "Role", "Type", "Status", "Assigned At" }, rows));
    }
}
