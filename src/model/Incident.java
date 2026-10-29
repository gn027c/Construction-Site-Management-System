package model;

import static model.ModelValidation.*;
import exception.DataValidationException;
import java.time.LocalDateTime;
import util.DateTimeHelper;
import util.IdentifierHelper;

/**
 * Represents a safety incident and controls its assignment and resolution.
 *
 * <p>The lifecycle is {@code OPEN -> ASSIGNED -> RESOLVED}. An open incident has
 * no assignee; an assigned or resolved incident must retain an assignee code.
 * The service checks whether that code belongs to a registered person.
 */
public class Incident {
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

    private final String incidentId;
    private String title;
    private String description;
    private IncidentSeverity severity;
    private IncidentStatus status;
    private String assignedTo;
    private final String reportedAt;
    private String resolvedAt;
    private boolean archived;

    /**
     * Reconstitutes an incident while validating status and assignment together.
     *
     * <p>An open incident must have no assignee. Assigned and resolved incidents
     * must have a valid code; registry membership is checked by the service.
     */
    public Incident(
            String incidentId,
            String title,
            String description,
            IncidentSeverity severity,
            IncidentStatus status,
            String assignedTo
    ) {
        this(incidentId, title, description, severity, status, assignedTo, "", "", false);
    }

    /**
     * Restores persisted timestamps without inventing missing historical dates.
     * Archived incidents remain resolved and retain their original assignment.
     */
    public Incident(
            String incidentId,
            String title,
            String description,
            IncidentSeverity severity,
            IncidentStatus status,
            String assignedTo,
            String reportedAt,
            String resolvedAt,
            boolean archived
    ) {
        this.incidentId = IdentifierHelper.normalize(requiredText(incidentId, "incidentId"));
        this.title = requiredText(title, "title");
        this.description = requiredText(description, "description");
        this.severity = requiredValue(severity, "severity");
        this.status = requiredValue(status, "status");
        String assignee = optionalText(assignedTo);
        if (status == IncidentStatus.OPEN) {
            if (!assignee.isEmpty()) {
                throw new exception.DataValidationException("assignedTo", "Must be blank when status is OPEN.");
            }
            this.assignedTo = "";
        } else {
            this.assignedTo = personCode(assignee, "assignedTo");
        }
        this.reportedAt = optionalTimestamp(reportedAt, "reportedAt");
        this.resolvedAt = optionalTimestamp(resolvedAt, "resolvedAt");
        if (status != IncidentStatus.RESOLVED && (!this.resolvedAt.isEmpty() || archived)) {
            throw new DataValidationException(
                    "incident", "Only resolved incidents can have a resolution date or archive."
            );
        }
        if (!this.reportedAt.isEmpty() && !this.resolvedAt.isEmpty()
                && DateTimeHelper.parseTimestamp(this.resolvedAt).isBefore(
                        DateTimeHelper.parseTimestamp(this.reportedAt)
                )) {
            throw new DataValidationException("resolvedAt", "Must not precede the reported time.");
        }
        this.archived = archived;
    }

    private static String optionalTimestamp(String value, String field) {
        requiredValue(value, field);
        if (value.isEmpty()) {
            return "";
        }
        return timestamp(value);
    }

    /**
     * Creates a new open incident without an assignee.
     */
    public static Incident report(String id, String title, String description, IncidentSeverity severity) {
        return new Incident(
                id, title, description, severity, IncidentStatus.OPEN, "",
                DateTimeHelper.formatTimestamp(LocalDateTime.now()), "", false
        );
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

    public String getReportedAt() {
        return reportedAt;
    }

    public String getResolvedAt() {
        return resolvedAt;
    }

    public boolean isArchived() {
        return archived;
    }

    public boolean isOpen() {
        return status == IncidentStatus.OPEN;
    }

    public boolean isResolved() {
        return status == IncidentStatus.RESOLVED;
    }

    public void setTitle(String title) {
        this.title = requiredText(title, "title");
    }

    public void setDescription(String description) {
        this.description = requiredText(description, "description");
    }

    public void setSeverity(IncidentSeverity severity) {
        this.severity = requiredValue(severity, "severity");
    }

    /**
     * Assigns an open incident after validating the supplied code.
     */
    public void assignTo(String code) {
        if (!isOpen()) {
            throw new exception.InvalidIncidentStateException(
                    incidentId,
                    status.name(),
                    "Only OPEN incidents can be assigned."
            );
        }
        String assignee = personCode(code, "assignedTo");
        assignedTo = assignee;
        status = IncidentStatus.ASSIGNED;
    }

    /**
     * Marks an assigned incident as resolved while retaining its assignee.
     */
    public void resolve() {
        if (status != IncidentStatus.ASSIGNED) {
            throw new exception.InvalidIncidentStateException(
                    incidentId,
                    status.name(),
                    "Only ASSIGNED incidents can be resolved."
            );
        }
        String resolutionTime = DateTimeHelper.formatTimestamp(LocalDateTime.now());
        if (!reportedAt.isEmpty()
                && DateTimeHelper.parseTimestamp(resolutionTime).isBefore(DateTimeHelper.parseTimestamp(reportedAt))) {
            throw new DataValidationException("resolvedAt", "System time precedes the incident reporting time.");
        }
        resolvedAt = resolutionTime;
        status = IncidentStatus.RESOLVED;
    }

    /**
     * Archives a resolved incident without deleting it from history or reports.
     */
    public boolean archive() {
        if (!isResolved()) {
            throw new exception.InvalidIncidentStateException(
                    incidentId, status.name(), "Only RESOLVED incidents can be archived."
            );
        }
        if (archived) {
            return false;
        }
        archived = true;
        return true;
    }

    public String toCsvLine() {
        return util.CsvHelper.toCsvLine(
                incidentId, title, description, severity.name(), status.name(), assignedTo,
                reportedAt, resolvedAt, Boolean.toString(archived)
        );
    }

    @Override
    public String toString() {
        return String.format(
                "Incident{incidentId='%s', title='%s', severity=%s, status=%s, assignedTo='%s', archived=%s}",
                incidentId,
                title,
                severity,
                status,
                assignedTo.isEmpty() ? "None" : assignedTo,
                archived
        );
    }
}
