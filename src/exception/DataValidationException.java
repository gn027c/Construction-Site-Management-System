package exception;

public class DataValidationException extends SiteManagementException {
    public DataValidationException(String message) {
        super(message);
    }

    public DataValidationException(String fieldName, String reason) {
        super("Invalid value for field [" + fieldName + "]: " + reason);
    }
}
