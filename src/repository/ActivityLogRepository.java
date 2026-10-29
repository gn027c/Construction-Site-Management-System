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
import model.ActivityEvent;
import util.CsvHelper;
import util.DateTimeHelper;

/**
 * Persists activity records without replacing malformed or unreadable history.
 * Each write reloads the current file and publishes new records only after a successful atomic replacement.
 */
public final class ActivityLogRepository {
    private static final String HEADER =
            "logId,operationId,timestamp,actorUsername,actorRole,action,targetType,targetId,status,reason";
    private final Path file;
    private List<ActivityEvent> events = new ArrayList<>();
    private String lastError = "";

    public ActivityLogRepository(Path file) {
        this.file = file;
        reload();
    }

    public synchronized boolean reload() {
        if (!Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
            events = new ArrayList<>();
            lastError = "";
            return true;
        }
        try {
            List<String[]> rows = CsvHelper.readRows(file.toString());
            if (rows.isEmpty() || !Arrays.equals(rows.get(0), HEADER.split(","))) {
                throw new IllegalArgumentException("Invalid activity header.");
            }
            List<ActivityEvent> staged = new ArrayList<>();
            Set<String> identifiers = new HashSet<>();
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (CsvHelper.isBlankRow(row)) {
                    continue;
                }
                if (row.length != 10 || !identifiers.add(row[0])) {
                    throw new IllegalArgumentException("Invalid activity row " + (i + 1) + ".");
                }
                staged.add(new ActivityEvent(
                        row[0], row[1], row[2], row[3], row[4], row[5], row[6], row[7], row[8], row[9]
                ));
            }
            events = staged;
            lastError = "";
            return true;
        } catch (RuntimeException e) {
            lastError = "Cannot read activity_log.csv; existing history was preserved. " + e.getMessage();
            return false;
        }
    }

    public synchronized ActivityEvent record(
            String actorUsername, String actorRole, String action,
            String targetType, String targetId, String status, String reason
    ) {
        if (!reload()) {
            return null;
        }
        try {
            long number = 0;
            for (ActivityEvent event : events) {
                number = Math.max(number, Long.parseLong(event.getLogId().substring(3)));
            }
            number = Math.addExact(number, 1);
            ActivityEvent event = new ActivityEvent(
                    "LOG" + number, "OP" + number, DateTimeHelper.formatTimestamp(LocalDateTime.now()),
                    actorUsername, actorRole, action, targetType, targetId, status, reason
            );
            List<String> rows = new ArrayList<>();
            rows.add(HEADER);
            for (ActivityEvent existing : events) {
                rows.add(existing.toCsvLine());
            }
            rows.add(event.toCsvLine());
            if (!CsvHelper.writeAllLines(file.toString(), rows)) {
                lastError = "Cannot write activity_log.csv; the business result is unchanged.";
                return null;
            }
            events.add(event);
            lastError = "";
            return event;
        } catch (RuntimeException e) {
            lastError = "Cannot record activity: " + e.getMessage();
            return null;
        }
    }

    public synchronized List<ActivityEvent> findAll() {
        return new ArrayList<>(events);
    }

    public synchronized String getLastError() {
        return lastError;
    }
}
