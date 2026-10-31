package exception;

public class AuthenticationException extends SiteManagementException {
    public AuthenticationException() {
        super("Invalid username or password, or account disabled.");
    }
}
