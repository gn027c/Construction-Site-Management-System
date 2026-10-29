package exception;

/**
 * Reports an operation that violates the incident lifecycle.
 *
 * <p>The supported transition order is {@code OPEN -> ASSIGNED -> RESOLVED}.
 *
 * <p>Models and services decide when to raise this error; the exception class
 * only carries the error to the caller.
 */
public class InvalidIncidentStateException extends SiteManagementException {
    public InvalidIncidentStateException(String incidentId, String currentState, String reason) {
        super("Invalid state for incident [" + incidentId + "] (status=" + currentState + "): " + reason);
    }
}
