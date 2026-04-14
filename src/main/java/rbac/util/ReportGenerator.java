package rbac.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import rbac.manager.AssignmentManager;
import rbac.manager.RoleManager;
import rbac.manager.UserManager;
import rbac.model.Permission;
import rbac.model.Role;
import rbac.model.RoleAssignment;
import rbac.model.User;

public final class ReportGenerator {

    private ReportGenerator() {}

    public static String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        List<User> users = userManager.findAll();
        List<String> lines = users.parallelStream()
            .map(u -> {
                String header = String.format("User: %s (%s) <%s>%n", u.username(), u.fullName(), u.email());
                Set<String> roles = assignmentManager.findByUser(u).stream()
                    .filter(RoleAssignment::isActive)
                    .map(a -> a.role().getName())
                    .collect(Collectors.toSet());
                String roleLine = "  Roles: " + (roles.isEmpty() ? "-" : String.join(", ", roles)) + "\n";
                return new AbstractMap.SimpleEntry<>(u.username(), header + roleLine);
            })
            .collect(Collectors.toCollection(ArrayList::new))
            .stream()
            .sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()))
            .map(Map.Entry::getValue)
            .toList();

        StringBuilder sb = new StringBuilder();
        sb.append(FormatUtils.formatHeader("User report"));
        for (String s : lines) sb.append(s);
        return sb.toString();
    }

    public static String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append(FormatUtils.formatHeader("Role report"));
        for (Role r : roleManager.findAll()) {
            long cnt = assignmentManager.findByRole(r).stream().filter(RoleAssignment::isActive).map(a -> a.user().username()).distinct().count();
            sb.append(String.format("Role: %s — %d user(s)%n", r.getName(), cnt));
        }
        return sb.toString();
    }

    public static String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        List<User> usersList = userManager.findAll();

        Set<String> resources = new ConcurrentSkipListSet<>();
        Map<String, Set<String>> userToResources = new ConcurrentHashMap<>();

        usersList.parallelStream().forEach(u -> {
            Set<Permission> perms = assignmentManager.getUserPermissions(u);
            Set<String> resSet = new HashSet<>();
            for (Permission p : perms) {
                resources.add(p.resource());
                resSet.add(p.resource());
            }
            userToResources.put(u.username(), resSet);
        });

        var resList = resources.stream().sorted().toList();
        var users = usersList.stream().map(User::username).sorted().toList();
        String[] headers = new String[1 + resList.size()];
        headers[0] = "User";
        for (int i = 0; i < resList.size(); i++) headers[i + 1] = resList.get(i);
        List<String[]> rows = new ArrayList<>();
        for (String un : users) {
            String[] row = new String[headers.length];
            row[0] = un;
            Set<String> rs = userToResources.getOrDefault(un, Set.of());
            for (int i = 0; i < resList.size(); i++) {
                row[i + 1] = rs.contains(resList.get(i)) ? "X" : "";
            }
            rows.add(row);
        }
        return FormatUtils.formatTable(headers, rows);
    }

    public static void exportToFile(String report, String filename) throws java.io.IOException {
        java.nio.file.Files.writeString(java.nio.file.Path.of(filename), report != null ? report : "");
    }
}
