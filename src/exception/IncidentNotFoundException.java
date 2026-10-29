package exception;

/**
 * Reports an incident identifier that cannot be found for the requested operation.
 *
 * <p>Models and services decide when to raise this error; the exception class
 * only carries the error to the caller.
 *
 * <p>Responsibility: Nguyen Tan Loi (SE211059) - module exceptions.
 */
public class IncidentNotFoundException extends SiteManagementException {
    public IncidentNotFoundException(String incidentId) {
        super("Incident record not found with ID: " + incidentId);
    }
}
