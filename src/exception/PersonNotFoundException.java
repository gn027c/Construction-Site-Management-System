package exception;

public class PersonNotFoundException extends SiteManagementException {
    public PersonNotFoundException(String code) {
        super("Personnel record not found with code: " + code);
    }
}
