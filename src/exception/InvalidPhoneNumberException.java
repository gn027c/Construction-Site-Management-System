package exception;

public class InvalidPhoneNumberException extends DataValidationException {
    public InvalidPhoneNumberException(String phone) {
        super("phoneNumber", "Must contain 10 digits and start with 0.");
    }
}
