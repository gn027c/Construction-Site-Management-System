package exception;

/**
 * Reports a zone identifier that cannot be found for the requested operation.
 *
 * <p>Models and services decide when to raise this error; the exception class
 * only carries the error to the caller.
 */
public class ZoneNotFoundException extends SiteManagementException {
    public ZoneNotFoundException(String zoneId) {
        super("Construction zone not found with ID: " + zoneId);
    }
}
