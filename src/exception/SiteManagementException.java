package exception;

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
