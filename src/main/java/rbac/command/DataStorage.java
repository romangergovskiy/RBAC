package rbac.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rbac.RBACSystem;
import rbac.model.AssignmentMetadata;
import rbac.model.Permission;
import rbac.model.PermanentAssignment;
import rbac.model.Role;
import rbac.model.RoleAssignment;
import rbac.model.TemporaryAssignment;
import rbac.model.User;

public final class DataStorage {

    private DataStorage() {}

    public static void save(RBACSystem sys, String path) throws IOException {
        var sb = new StringBuilder();
        for (var u : sys.getUserManager().findAll()) {
            sb.append("USER\t").append(u.username()).append("\t").append(u.fullName()).append("\t").append(u.email()).append("\n");
        }
        for (var r : sys.getRoleManager().findAll()) {
            sb.append("ROLE\t").append(r.getId()).append("\t").append(r.getName()).append("\t").append(r.getDescription()).append("\n");
            for (var p : r.getPermissions()) {
                sb.append("PERM\t").append(r.getId()).append("\t").append(p.name()).append("\t").append(p.resource()).append("\t").append(p.description().replace("\t", " ")).append("\n");
            }
        }
        for (var a : sys.getAssignmentManager().findAll()) {
            String line = "ASSIGN\t" + a.assignmentId() + "\t" + a.user().username() + "\t" + a.role().getId() + "\t" + a.assignmentType() + "\t"
                + a.metadata().assignedBy() + "\t" + a.metadata().assignedAt() + "\t" + (a.metadata().reason() != null ? a.metadata().reason().replace("\t", " ") : "");
            if (a instanceof TemporaryAssignment ta) {
                line += "\t" + ta.getExpiresAt() + "\t" + ta.isActive();
            } else if (a instanceof PermanentAssignment pa) {
                line += "\t" + pa.isRevoked();
            }
            sb.append(line).append("\n");
        }
        Files.writeString(Path.of(path), sb.toString());
    }

    public static void load(RBACSystem sys, String path) throws IOException {
        sys.getUserManager().clear();
        sys.getRoleManager().clear();
        sys.getAssignmentManager().clear();

        Map<String, User> users = new HashMap<>();
        Map<String, Role> roles = new HashMap<>();
        List<String> assignLines = new ArrayList<>();

        for (String line : Files.readAllLines(Path.of(path))) {
            if (line.isBlank()) continue;
            String[] parts = line.split("\t", -1);
            if (parts[0].equals("USER") && parts.length >= 4) {
                User u = User.create(parts[1], parts[2], parts[3]);
                sys.getUserManager().add(u);
                users.put(parts[1], u);
            } else if (parts[0].equals("ROLE") && parts.length >= 4) {
                Role r = new Role(parts[1], parts[2], parts[3], new HashSet<>());
                sys.getRoleManager().add(r);
                roles.put(parts[1], r);
            } else if (parts[0].equals("PERM") && parts.length >= 5) {
                Role r = roles.get(parts[1]);
                if (r != null) r.addPermission(new Permission(parts[2], parts[3], parts[4]));
            } else if (parts[0].equals("ASSIGN")) {
                assignLines.add(line);
            }
        }

        for (String line : assignLines) {
            String[] parts = line.split("\t", -1);
            if (parts.length < 8) continue;
            User u = users.get(parts[2]);
            Role r = roles.get(parts[3]);
            if (u == null || r == null) continue;
            String reason = parts.length > 7 && !parts[7].isEmpty() ? parts[7] : null;
            AssignmentMetadata meta = new AssignmentMetadata(parts[5], parts[6], reason);
            RoleAssignment ra;
            if ("TEMPORARY".equals(parts[4]) && parts.length >= 10) {
                ra = new TemporaryAssignment(u, r, meta, parts[9], false);
            } else {
                ra = new PermanentAssignment(u, r, meta);
                if (parts.length >= 9 && "true".equals(parts[8])) ((PermanentAssignment) ra).revoke();
            }
            try { sys.getAssignmentManager().add(ra); } catch (Exception ignored) {}
        }
    }
}
