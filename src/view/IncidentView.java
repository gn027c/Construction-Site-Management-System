package view;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import model.incident.Incident;

/**
 * Formats safety incident fields consistently for searchable lists and detail screens.
 */
public final class IncidentView {
    public static final List<String> TABLE_HEADERS = Arrays.asList(
            "Incident ID", "Title", "Severity", "Status", "Assigned To", "Reported", "Archived"
    );

    private IncidentView() {
    }

    public static List<String> tableRow(Incident incident) {
        return Arrays.asList(
                incident.getIncidentId(),
                incident.getTitle(),
                incident.getSeverity().name(),
                incident.getStatus().name(),
                incident.getAssignedTo().isEmpty() ? "Unassigned" : incident.getAssignedTo(),
                incident.getReportedAt().isEmpty() ? "-" : incident.getReportedAt(),
                incident.isArchived() ? "Yes" : "No"
        );
    }

    public static String formatSummary(Incident incident) {
        StringJoiner summary = new StringJoiner(" | ");
        for (Map.Entry<String, String> field : fields(incident).entrySet()) {
            summary.add(field.getKey() + ": " + field.getValue());
        }
        return summary.toString();
    }

    public static void showDetails(Incident incident) {
        ConsoleTheme.title("INCIDENT DETAILS");
        for (Map.Entry<String, String> field : fields(incident).entrySet()) {
            System.out.println(field.getKey() + ": " + field.getValue());
        }
    }

    private static Map<String, String> fields(Incident incident) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("Incident ID", incident.getIncidentId());
        fields.put("Title", incident.getTitle());
        fields.put("Severity", incident.getSeverity().name());
        fields.put("Status", incident.getStatus().name());
        fields.put("Assigned to", incident.getAssignedTo().isEmpty() ? "Unassigned" : incident.getAssignedTo());
        if (!incident.getReportedAt().isEmpty()) {
            fields.put("Reported", incident.getReportedAt());
        }
        if (!incident.getResolvedAt().isEmpty()) {
            fields.put("Resolved", incident.getResolvedAt());
        }
        fields.put("Archived", incident.isArchived() ? "Yes" : "No");
        return fields;
    }
}

