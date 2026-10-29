package model;

import static model.ModelValidation.*;

/**
 * NHIỆM VỤ: Thành viên 4 (Nguyễn Tấn Lợi - SE211059)
 * MÔ TẢ:
 * - Lớp thực thể đại diện cho hồ sơ sự cố an toàn lao động phát sinh tại hiện trường.
 * - Chịu trách nhiệm quản lý thông tin sự cố, mức độ nguy hiểm, trạng thái xử lý và nhân sự liên quan.
 * - Cung cấp các hành vi phân công xử lý và cập nhật tiến độ giải quyết sự cố.
 */
public class Incident {
    public enum IncidentSeverity { LOW, MEDIUM, HIGH, CRITICAL }
    public enum IncidentStatus { OPEN, ASSIGNED, RESOLVED }

    private final String incidentId;
    private String title;
    private String description;
    private IncidentSeverity severity;
    private IncidentStatus status;
    private String assignedTo;

    /** Reconstitute a saved incident, validating status and assignment together. */
    public Incident(String incidentId, String title, String description,
                    IncidentSeverity severity, IncidentStatus status, String assignedTo) {
        this.incidentId = requiredText(incidentId, "incidentId");
        this.title = requiredText(title, "title");
        this.description = requiredText(description, "description");
        this.severity = requiredValue(severity, "severity");
        this.status = requiredValue(status, "status");
        String assignee = optionalText(assignedTo);
        if (status == IncidentStatus.OPEN) {
            if (!assignee.isEmpty()) throw new exception.DataValidationException("assignedTo", "Must be blank when status is OPEN.");
            this.assignedTo = "";
        } else {
            this.assignedTo = personCode(assignee, "assignedTo");
        }
    }

    public static Incident report(String id, String title, String description, IncidentSeverity severity) {
        return new Incident(id, title, description, severity, IncidentStatus.OPEN, "");
    }

    public String getIncidentId() { return incidentId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public IncidentSeverity getSeverity() { return severity; }
    public IncidentStatus getStatus() { return status; }
    public String getAssignedTo() { return assignedTo; }
    public boolean isOpen() { return status == IncidentStatus.OPEN; }
    public boolean isResolved() { return status == IncidentStatus.RESOLVED; }

    public void setTitle(String title) { this.title = requiredText(title, "title"); }
    public void setDescription(String description) { this.description = requiredText(description, "description"); }
    public void setSeverity(IncidentSeverity severity) { this.severity = requiredValue(severity, "severity"); }

    /** The service verifies registry membership; this entity owns the lifecycle rule. */
    public void assignTo(String code) {
        if (!isOpen()) throw new exception.InvalidIncidentStateException(incidentId, status.name(), "Only OPEN incidents can be assigned.");
        String assignee = personCode(code, "assignedTo");
        assignedTo = assignee;
        status = IncidentStatus.ASSIGNED;
    }

    public void resolve() {
        if (status != IncidentStatus.ASSIGNED)
            throw new exception.InvalidIncidentStateException(incidentId, status.name(), "Only ASSIGNED incidents can be resolved.");
        status = IncidentStatus.RESOLVED;
    }

    public String toCsvLine() {
        return util.CsvHelper.toCsvLine(incidentId, title, description, severity.name(), status.name(), assignedTo);
    }

    @Override public String toString() {
        return String.format("Incident{incidentId='%s', title='%s', severity=%s, status=%s, assignedTo='%s'}",
                incidentId, title, severity, status, assignedTo.isEmpty() ? "None" : assignedTo);
    }
}
