package repository;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.Notification;
import util.CsvHelper;
import util.DateTimeHelper;

/**
 * Persists account notifications and read flags while retaining malformed source files for diagnosis.
 */
public final class NotificationRepository {
    private static final String HEADER = "notificationId,recipientUsername,operationId,timestamp,message,read";
    private final Path file;
    private List<Notification> notifications = new ArrayList<>();
    private String lastError = "";

    public NotificationRepository(Path file) {
        this.file = file;
        reload();
    }

    public synchronized boolean reload() {
        if (!Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
            notifications = new ArrayList<>();
            lastError = "";
            return true;
        }
        try {
            List<String[]> rows = CsvHelper.readRows(file.toString());
            if (rows.isEmpty() || !Arrays.equals(rows.get(0), HEADER.split(","))) {
                throw new IllegalArgumentException("Invalid notification header.");
            }
            List<Notification> staged = new ArrayList<>();
            Set<String> identifiers = new HashSet<>();
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (CsvHelper.isBlankRow(row)) {
                    continue;
                }
                if (row.length != 6 || !identifiers.add(row[0])
                        || !(row[5].equals("true") || row[5].equals("false"))) {
                    throw new IllegalArgumentException("Invalid notification row " + (i + 1) + ".");
                }
                staged.add(new Notification(row[0], row[1], row[2], row[3], row[4], Boolean.parseBoolean(row[5])));
            }
            notifications = staged;
            lastError = "";
            return true;
        } catch (RuntimeException e) {
            lastError = "Cannot read notifications.csv; existing messages were preserved. " + e.getMessage();
            return false;
        }
    }

    public synchronized boolean add(String username, String operationId, String message) {
        if (!reload()) {
            return false;
        }
        try {
            long number = 0;
            for (Notification notification : notifications) {
                number = Math.max(number, Long.parseLong(notification.getNotificationId().substring(3)));
            }
            number = Math.addExact(number, 1);
            List<Notification> staged = new ArrayList<>(notifications);
            staged.add(new Notification(
                    "NTF" + number, username, operationId,
                    DateTimeHelper.formatTimestamp(LocalDateTime.now()), message, false
            ));
            return save(staged);
        } catch (RuntimeException e) {
            lastError = "Cannot save notification: " + e.getMessage();
            return false;
        }
    }

    public synchronized boolean markRead(String username, String notificationId) {
        if (!reload()) {
            return false;
        }
        List<Notification> staged = new ArrayList<>(notifications);
        for (int i = 0; i < staged.size(); i++) {
            Notification notification = staged.get(i);
            if (notification.getNotificationId().equals(notificationId)
                    && notification.getRecipientUsername().equals(username)) {
                if (notification.isRead()) {
                    lastError = "";
                    return true;
                }
                staged.set(i, notification.asRead());
                return save(staged);
            }
        }
        lastError = "Notification not found for the signed-in account.";
        return false;
    }

    private boolean save(List<Notification> staged) {
        List<String> rows = new ArrayList<>();
        rows.add(HEADER);
        for (Notification notification : staged) {
            rows.add(notification.toCsvLine());
        }
        if (!CsvHelper.writeAllLines(file.toString(), rows)) {
            lastError = "Cannot write notifications.csv; the business result is unchanged.";
            return false;
        }
        notifications = staged;
        lastError = "";
        return true;
    }

    public synchronized List<Notification> findAll() {
        return new ArrayList<>(notifications);
    }

    public synchronized String getLastError() {
        return lastError;
    }
}
