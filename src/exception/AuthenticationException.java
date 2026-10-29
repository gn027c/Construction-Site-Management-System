package exception;

/**
 * Reports failed login without revealing whether the account exists or is disabled.
 */
public class AuthenticationException extends SiteManagementException {
    public AuthenticationException() {
        super("Invalid username or password, or account disabled.");
    }
}
