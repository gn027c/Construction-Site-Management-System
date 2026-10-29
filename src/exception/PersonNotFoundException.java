package exception;

/**
 * Reports missing personnel when the operation requires an existing record.
 *
 * <p>Models and services decide when to raise this error; the exception class
 * only carries the error to the caller.
 *
 * <p>Responsibility: Tran Ngoc Anh Tuan (SE201513) - module exceptions.
 */
public class PersonNotFoundException extends SiteManagementException {
    public PersonNotFoundException(String code) {
        super("Personnel record not found with code: " + code);
    }
}
