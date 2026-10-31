package model.person;

import static model.common.ModelValidation.optionalText;
import java.util.Objects;

public final class SafetyCertificate {
    public static final SafetyCertificate EMPTY = new SafetyCertificate("");

    private final String certNumber;

    public SafetyCertificate(String certNumber) {
        this.certNumber = optionalText(certNumber);
    }

    public String getCertNumber() {
        return certNumber;
    }

    public boolean isValid() {
        return certNumber.endsWith("_SAFE");
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SafetyCertificate
                && certNumber.equals(((SafetyCertificate) other).certNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certNumber);
    }

    @Override
    public String toString() {
        return String.format("SafetyCertificate[number='%s', valid=%s]", certNumber, isValid());
    }
}
