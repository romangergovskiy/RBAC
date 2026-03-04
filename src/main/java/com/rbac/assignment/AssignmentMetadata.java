package com.rbac.assignment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static AssignmentMetadata now(String assignedBy, String reason) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        return new AssignmentMetadata(assignedBy, timestamp, reason);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Assigned by: %s\n", assignedBy));
        sb.append(String.format("Assigned at: %s\n", assignedAt));
        if (reason != null && !reason.isBlank()) sb.append(String.format("Reason: %s", reason));
        return sb.toString();
    }
}
