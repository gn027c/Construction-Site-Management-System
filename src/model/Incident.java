package model;

import exception.AppError;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Incident {
    private static final DateTimeFormatter REPORT_TIME = util.DateHelper.TIMESTAMP_FORMATTER;

    public enum IncidentSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum IncidentStatus {
        OPEN,
        ASSIGNED,
        RESOLVED
    }

    private String incidentId;
    private String title;
    private String description;
    private IncidentSeverity severity;
    private IncidentStatus status;
    private String assignedTo;
    private final LocalDateTime reportedAt;

    public Incident(String incidentId, String title, String description,
                    IncidentSeverity severity, IncidentStatus status, String assignedTo) {
        this(incidentId, title, description, severity, status, assignedTo, LocalDateTime.now());
    }

    public Incident(String incidentId, String title, String description,
                    IncidentSeverity severity, IncidentStatus status, String assignedTo,
                    LocalDateTime reportedAt) {
        this.incidentId = util.ValidationHelper.requireText(incidentId, "Mã sự cố (incidentId) không được để trống.");
        this.title = util.ValidationHelper.requireText(title, "Tiêu đề sự cố không được để trống.");
        this.description = util.ValidationHelper.requireText(description, "Mô tả sự cố không được để trống.");
        this.severity = util.ValidationHelper.requireNotNull(severity, "Mức độ nghiêm trọng không được để trống.");
        this.status = util.ValidationHelper.requireNotNull(status, "Trạng thái sự cố không được để trống.");
        String assignee = (assignedTo != null) ? assignedTo.trim() : "";
        if ((status == IncidentStatus.OPEN) != assignee.isEmpty()) {
            throw new exception.DataValidationException(
                    "assignedTo", "An OPEN incident has no handler; any other status needs one.");
        }
        this.assignedTo = assignee;
        this.reportedAt = reportedAt;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public LocalDateTime getReportedAt() {
        return reportedAt;
    }

    public String getReportedAtText() {
        return reportedAt == null ? "" : reportedAt.format(REPORT_TIME);
    }

    public void assignTo(Person handler) {
        if (status == IncidentStatus.RESOLVED) {
            throw new AppError("Invalid State Transition for Incident [" + incidentId + "] currently in state ["
                    + status.name() + "]: A resolved incident cannot be assigned again.");
        }
        util.ValidationHelper.requireNotNull(handler, "Handler cannot be null.");
        if (!handler.canHandleIncidents()) {
            throw new AppError("Person with code '" + handler.getCode() + "' is not eligible to handle incidents.");
        }
        this.assignedTo = handler.getCode();
        this.status = IncidentStatus.ASSIGNED;
    }

    public void resolve() {
        if (status != IncidentStatus.ASSIGNED) {
            throw new AppError("Invalid State Transition for Incident [" + incidentId + "] currently in state ["
                    + status.name() + "]: Only ASSIGNED incidents can be transitioned to RESOLVED.");
        }
        this.status = IncidentStatus.RESOLVED;
    }

    public String toCsvLine() {
        return util.CsvHelper.toCsvLine(
                incidentId,
                title,
                description,
                severity != null ? severity.name() : "",
                status != null ? status.name() : "",
                assignedTo != null ? assignedTo : "",
                getReportedAtText());
    }

    @Override
    public String toString() {
        return String.format("Incident{incidentId='%s', title='%s', severity=%s, status=%s, assignedTo='%s', reportedAt='%s'}",
                incidentId, title, severity, status, assignedTo.isEmpty() ? "None" : assignedTo,
                getReportedAtText().isEmpty() ? "Unknown" : getReportedAtText());
    }
}
