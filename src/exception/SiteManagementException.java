package exception;

/**
 * Provides the unchecked base exception for SmartSite domain and application errors.
 *
 * <p>Subclasses describe specific failures. Models and services decide when to
 * raise them; console controllers decide how to present them to the user.
 */
public class SiteManagementException extends RuntimeException {
    public SiteManagementException() {
        super("An error occurred in SmartSite Management System.");
    }

    public SiteManagementException(String message) {
        super(message);
    }

    public SiteManagementException(String message, Throwable cause) {
        super(message, cause);
    }
}
