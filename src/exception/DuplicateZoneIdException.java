package exception;

/**
 * Reports an attempt to register a zone identifier that already exists.
 *
 * <p>Models and services decide when to raise this error; the exception class
 * only carries the error to the caller.
 *
 * <p>Responsibility: Le Tan Thien (SE201852) - module exceptions.
 */
public class DuplicateZoneIdException extends SiteManagementException {
    public DuplicateZoneIdException(String zoneId) {
        super("Zone ID already exists in the system: " + zoneId);
    }
}
