package service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.attendance.AttendanceRecord;
import util.DateTimeHelper;

/**
 * Scans stored attendance in insertion order without changing historical records.
 * Issues identify logical CSV rows, personnel and the preceding record.
 */
public final class AttendanceIntegrityService {
    public List<String> scan(List<AttendanceRecord> records) {
        List<String> issues = new ArrayList<>();
        Map<String, AttendanceRecord> previous = new HashMap<>();
        for (int i = 0; i < records.size(); i++) {
            AttendanceRecord current = records.get(i);
            AttendanceRecord last = previous.get(current.getPersonCode());
            try {
                current.assertCanFollow(last);
                if (last != null && DateTimeHelper.parseTimestamp(current.getTimestamp()).isBefore(
                        DateTimeHelper.parseTimestamp(last.getTimestamp())
                )) {
                    throw new IllegalArgumentException("Timestamp precedes " + last.getRecordId() + ".");
                }
            } catch (RuntimeException e) {
                issues.add("CSV record row " + (i + 2) + " / " + current.getRecordId()
                        + " / " + current.getPersonCode() + " / previous="
                        + (last == null ? "none" : last.getRecordId()) + ": " + e.getMessage());
            }
            previous.put(current.getPersonCode(), current);
        }
        return issues;
    }
}
