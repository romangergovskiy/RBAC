package rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static AssignmentMetadata now(String assignedBy, String reason) {
        if (assignedBy == null || assignedBy.isBlank()) {
            throw new IllegalArgumentException("assignedBy must be non-empty");
        }
        String at = LocalDateTime.now().format(FORMATTER);
        return new AssignmentMetadata(assignedBy, at, reason);
    }

    public AssignmentMetadata {
        if (assignedBy == null || assignedBy.isBlank()) {
            throw new IllegalArgumentException("assignedBy must be non-empty");
        }
        if (assignedAt == null || assignedAt.isBlank()) {
            throw new IllegalArgumentException("assignedAt must be non-empty");
        }
    }

    public String format() {
        return "AssignedBy: " + assignedBy + "\n"
            + "AssignedAt: " + assignedAt + "\n"
            + (reason == null || reason.isBlank() ? "Reason: (none)" : "Reason: " + reason);
    }
}

