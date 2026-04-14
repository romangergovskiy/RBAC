package rbac.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuditLog {
    private final List<AuditEntry> entries = new ArrayList<>();

    public void log(String action, String performer, String target, String details) {
        String ts = DateUtils.getCurrentDateTime();
        entries.add(new AuditEntry(ts, action, performer != null ? performer : "system", target != null ? target : "-", details != null ? details : ""));
    }

    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }

    public List<AuditEntry> getByPerformer(String performer) {
        return entries.stream().filter(e -> performer != null && performer.equals(e.performer())).collect(Collectors.toList());
    }

    public List<AuditEntry> getByAction(String action) {
        return entries.stream().filter(e -> action != null && action.equals(e.action())).collect(Collectors.toList());
    }

    public void printLog() {
        System.out.println(FormatUtils.formatHeader("Audit log"));
        for (AuditEntry e : entries) {
            System.out.printf("[%s] %s by %s on %s — %s%n", e.timestamp(), e.action(), e.performer(), e.target(), e.details());
        }
        if (entries.isEmpty()) System.out.println("(empty)");
    }

    public void saveToFile(String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (AuditEntry e : entries) {
            sb.append(e.timestamp()).append("\t").append(e.action()).append("\t").append(e.performer()).append("\t")
                .append(e.target().replace("\t", " ")).append("\t").append(e.details().replace("\t", " ")).append("\n");
        }
        Files.writeString(Path.of(filename), sb.toString());
    }

    public void shutdown() {
        // no-op (kept for API symmetry with async implementation)
    }
}
