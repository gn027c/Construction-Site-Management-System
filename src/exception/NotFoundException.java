package exception;

public class NotFoundException extends SiteManagementException {
    public NotFoundException(String kind, String idLabel, String id) {
        super(kind + " not found with " + idLabel + ": " + id);
    }
}
