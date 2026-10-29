package exception;

/**
 * Reports missing personnel when the operation requires an existing record.
 *
 * <p>Models and services decide when to raise this error; the exception class
 * only carries the error to the caller.
 */
public class PersonNotFoundException extends SiteManagementException {
    public PersonNotFoundException(String code) {
        super("Personnel record not found with code: " + code);
    }
}
