package model;

import static model.ModelValidation.*;

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
        this.incidentId = requiredText(incidentId, "incidentId");
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
    }

    /**
     * Creates a new open incident without an assignee.
     */
    public static Incident report(String id, String title, String description, IncidentSeverity severity) {
        return new Incident(id, title, description, severity, IncidentStatus.OPEN, "");
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
        status = IncidentStatus.RESOLVED;
    }

    public String toCsvLine() {
        return util.CsvHelper.toCsvLine(incidentId, title, description, severity.name(), status.name(), assignedTo);
    }

    @Override
    public String toString() {
        return String.format(
                "Incident{incidentId='%s', title='%s', severity=%s, status=%s, assignedTo='%s'}",
                incidentId,
                title,
                severity,
                status,
                assignedTo.isEmpty() ? "None" : assignedTo
        );
    }
}
