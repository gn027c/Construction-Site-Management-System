package model;

import java.util.Locale;
import util.CsvHelper;
import util.DateTimeHelper;

/**
 * Captures one completed application action without storing credentials or raw password input.
 * Business success and activity-log storage success are independent results.
 */
public final class ActivityEvent {
    private final String logId;
    private final String operationId;
    private final String timestamp;
    private final String actorUsername;
    private final String actorRole;
    private final String action;
    private final String targetType;
    private final String targetId;
    private final String status;
    private final String reason;

    public ActivityEvent(
            String logId, String operationId, String timestamp, String actorUsername, String actorRole,
            String action, String targetType, String targetId, String status, String reason
    ) {
        if (logId == null || !logId.matches("LOG[1-9][0-9]*")) {
            throw new IllegalArgumentException("Invalid activity identifier.");
        }
        this.logId = logId;
        this.operationId = required(operationId, "operationId");
        DateTimeHelper.parseTimestamp(timestamp);
        this.timestamp = timestamp;
        this.actorUsername = required(actorUsername, "actorUsername");
        this.actorRole = optional(actorRole).isEmpty() ? "ANONYMOUS" : actorRole.trim().toUpperCase(Locale.ROOT);
        this.action = required(action, "action").toUpperCase(Locale.ROOT);
        this.targetType = optional(targetType).toUpperCase(Locale.ROOT);
        this.targetId = optional(targetId);
        this.status = required(status, "status").toUpperCase(Locale.ROOT);
        if (!this.status.equals("SUCCESS") && !this.status.equals("FAILED") && !this.status.equals("CANCELLED")) {
            throw new IllegalArgumentException("Unsupported activity status.");
        }
        this.reason = optional(reason);
        if (this.reason.contains("PBKDF2-SHA256$") || this.targetId.contains("PBKDF2-SHA256$")) {
            throw new IllegalArgumentException("Credential hashes must not appear in the activity log.");
        }
    }

    private static String required(String value, String field) {
        String normalized = optional(value);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Missing activity " + field + ".");
        }
        return normalized;
    }

    private static String optional(String value) {
        return value == null ? "" : value.trim();
    }

    public String getLogId() {
        return logId;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public String getActorRole() {
        return actorRole;
    }

    public String getAction() {
        return action;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public String toCsvLine() {
        return CsvHelper.toCsvLine(
                logId, operationId, timestamp, actorUsername, actorRole,
                action, targetType, targetId, status, reason
        );
    }

    @Override
    public String toString() {
        return logId + " | " + timestamp + " | " + actorUsername + " | " + action
                + " | " + targetType + " " + targetId + " | " + status + " | " + reason;
    }
}
