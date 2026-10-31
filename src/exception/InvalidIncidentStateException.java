package exception;

public class InvalidIncidentStateException extends SiteManagementException {
    public InvalidIncidentStateException(String incidentId, String currentState, String reason) {
        super("Invalid state for incident [" + incidentId + "] (status=" + currentState + "): " + reason);
    }
}
