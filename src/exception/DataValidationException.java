package exception;

/**
 * Reports invalid input or a violated model field constraint.
 *
 * <p>The field-and-reason constructor identifies the rejected field using a
 * shared message structure.
 *
 * <p>Models and services decide when to raise this error; the exception class
 * only carries the error to the caller.
 */
public class DataValidationException extends SiteManagementException {
    public DataValidationException(String message) {
        super(message);
    }

    public DataValidationException(String fieldName, String reason) {
        super("Invalid value for field [" + fieldName + "]: " + reason);
    }
}
