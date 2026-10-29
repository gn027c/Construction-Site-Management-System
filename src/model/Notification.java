package model;

import util.CsvHelper;
import util.DateTimeHelper;

/**
 * Stores a private message addressed to one login account with a persistent read flag.
 */
public final class Notification {
    private final String notificationId;
    private final String recipientUsername;
    private final String operationId;
    private final String timestamp;
    private final String message;
    private final boolean read;

    public Notification(
            String notificationId, String recipientUsername, String operationId,
            String timestamp, String message, boolean read
    ) {
        if (notificationId == null || !notificationId.matches("NTF[1-9][0-9]*")) {
            throw new IllegalArgumentException("Invalid notification identifier.");
        }
        this.notificationId = notificationId;
        this.recipientUsername = Account.normalizeUsername(recipientUsername);
        this.operationId = operationId == null ? "" : operationId.trim();
        DateTimeHelper.parseTimestamp(timestamp);
        this.timestamp = timestamp;
        if (message == null || message.trim().isEmpty() || message.contains("PBKDF2-SHA256$")) {
            throw new IllegalArgumentException("Notification message is missing or contains a credential hash.");
        }
        this.message = message.trim();
        this.read = read;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public String getOperationId() {
        return operationId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return read;
    }

    public Notification asRead() {
        return new Notification(notificationId, recipientUsername, operationId, timestamp, message, true);
    }

    public String toCsvLine() {
        return CsvHelper.toCsvLine(
                notificationId, recipientUsername, operationId, timestamp, message, Boolean.toString(read)
        );
    }

    @Override
    public String toString() {
        return notificationId + " | " + (read ? "Read" : "Unread") + " | " + timestamp + " | " + message;
    }
}
