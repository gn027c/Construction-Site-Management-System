package view;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import model.attendance.AttendanceRecord;

public final class AttendanceView {
    public static final List<String> TABLE_HEADERS = Arrays.asList(
            "Record ID", "Person", "Zone", "Type", "Timestamp"
    );

    private AttendanceView() {
    }

    public static List<String> tableRow(AttendanceRecord record) {
        return Arrays.asList(
                record.getRecordId(),
                record.getPersonCode(),
                record.getZoneId(),
                record.getCheckType().name(),
                record.getTimestamp()
        );
    }

    public static String formatSummary(AttendanceRecord record) {
        StringJoiner summary = new StringJoiner(" | ");
        for (Map.Entry<String, String> field : fields(record).entrySet()) {
            summary.add(field.getKey() + ": " + field.getValue());
        }
        return summary.toString();
    }

    public static void showDetails(AttendanceRecord record) {
        ConsoleTheme.title("ATTENDANCE RECORD DETAILS");
        for (Map.Entry<String, String> field : fields(record).entrySet()) {
            System.out.println(field.getKey() + ": " + field.getValue());
        }
    }

    private static Map<String, String> fields(AttendanceRecord record) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("Record ID", record.getRecordId());
        fields.put("Person", record.getPersonCode());
        fields.put("Zone", record.getZoneId());
        fields.put("Type", record.getCheckType().name());
        fields.put("Timestamp", record.getTimestamp());
        return fields;
    }
}
