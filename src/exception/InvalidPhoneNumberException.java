package exception;

/**
 * Reports a phone number that is not ten digits beginning with zero.
 *
 * <p>This subtype retains the shared field-and-reason message structure from
 * {@link DataValidationException}.
 *
 * <p>Models and services decide when to raise this error; the exception class
 * only carries the error to the caller.
 *
 * <p>Responsibility: Tran Ngoc Anh Tuan (SE201513) - module exceptions.
 */
public class InvalidPhoneNumberException extends DataValidationException {
    public InvalidPhoneNumberException(String phone) {
        super("phoneNumber", "Must contain 10 digits and start with 0.");
    }
}
