package exception;

public class IncidentNotFoundException extends SiteManagementException {
    public IncidentNotFoundException(String incidentId) {
        super("Incident record not found with ID: " + incidentId);
    }
}
