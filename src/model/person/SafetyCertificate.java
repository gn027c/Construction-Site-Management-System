package model.person;

import static model.common.ModelValidation.optionalText;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Value Object representing a worker or staff safety qualification certificate.
 *
 * <p>Encapsulates certificate identity, type, expiration date, revocation status,
 * and validity rules. Instances are immutable and compared by value.
 */
public final class SafetyCertificate {
    public static final SafetyCertificate EMPTY = new SafetyCertificate("", "NONE", null, false, false);

    private final String certNumber;
    private final String certType;
    private final LocalDate expiryDate;
    private final boolean revoked;
    private final boolean qualified;

    public SafetyCertificate(
            String certNumber,
            String certType,
            LocalDate expiryDate,
            boolean revoked,
            boolean qualified
    ) {
        this.certNumber = optionalText(certNumber);
        this.certType = (certType != null && !certType.trim().isEmpty()) ? certType.trim() : "GENERAL";
        this.expiryDate = expiryDate;
        this.revoked = revoked;
        this.qualified = qualified;
    }

    public SafetyCertificate(String certNumber, LocalDate expiryDate) {
        this(certNumber, "GENERAL", expiryDate, false, true);
    }

    /**
     * Factory method for creating a valid certified credential.
     */
    public static SafetyCertificate valid(String certNumber) {
        return new SafetyCertificate(certNumber, "GENERAL", LocalDate.now().plusYears(1), false, true);
    }

    /**
     * Factory method for creating an uncertified or failed credential.
     */
    public static SafetyCertificate uncertified(String certNumber) {
        return new SafetyCertificate(certNumber, "NONE", null, false, false);
    }

    /**
     * Builds a certificate from the stored certificate text. Text ending in
     * {@code _SAFE} is a valid certificate; any other text, such as {@code CERT_UNSAFE},
     * is kept but not valid. Expiry and revocation are not stored in CSV.
     */
    public static SafetyCertificate fromRaw(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return EMPTY;
        }
        String clean = raw.trim();
        if (clean.endsWith("_SAFE")) {
            return new SafetyCertificate(clean, "GENERAL", null, false, true);
        }
        return new SafetyCertificate(clean, "NONE", null, false, false);
    }

    public String getCertNumber() {
        return certNumber;
    }

    public String getCertType() {
        return certType;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isQualified() {
        return qualified;
    }

    /**
     * Evaluates domain business rules for validity:
     * Must be qualified, not revoked, not expired, and have a non-empty cert number.
     */
    public boolean isValid() {
        return qualified && !revoked && !isExpired() && !certNumber.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SafetyCertificate that = (SafetyCertificate) o;
        return revoked == that.revoked
                && qualified == that.qualified
                && Objects.equals(certNumber, that.certNumber)
                && Objects.equals(certType, that.certType)
                && Objects.equals(expiryDate, that.expiryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certNumber, certType, expiryDate, revoked, qualified);
    }

    @Override
    public String toString() {
        return String.format(
                "SafetyCertificate[number='%s', type='%s', expiry=%s, revoked=%s, valid=%s]",
                certNumber, certType, expiryDate, revoked, isValid()
        );
    }
}
