package model;

import static model.ModelValidation.*;
import java.io.Serializable;

/**
 * Represents one check-in or check-out for a person at a zone.
 *
 * <p>Record identity, person, zone and action are fixed after construction. The
 * entity validates timestamps and checks whether a new swipe can follow the
 * preceding swipe supplied by the service.
 *
 * <p>Responsibility: Nguyen Tan Loi (SE211059) - attendance records and swipe rules.
 */
public class AttendanceRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum CheckType {
        CHECK_IN,
        CHECK_OUT
    }

    private final String recordId;
    private final String personCode;
    private final String zoneId;
    private final CheckType checkType;
    private String timestamp;

    public AttendanceRecord(String recordId, String personCode, String zoneId, String timestamp, CheckType checkType) {
        this.recordId = requiredText(recordId, "recordId");
        this.personCode = personCode(personCode, "personCode");
        this.zoneId = requiredText(zoneId, "zoneId");
        this.timestamp = timestamp(timestamp);
        this.checkType = requiredValue(checkType, "checkType");
    }

    public String getRecordId() {
        return recordId;
    }

    public String getPersonCode() {
        return personCode;
    }

    public String getZoneId() {
        return zoneId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public CheckType getCheckType() {
        return checkType;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp(timestamp);
    }

    /**
     * Checks whether this swipe can follow the supplied swipe for the same person.
     *
     * <p>A first swipe must be a check-in. A check-in requires the previous swipe
     * to be a check-out; a check-out requires a check-in at the same zone. This
     * checks action order, not timestamp chronology or a complete history.
     */
    public void assertCanFollow(AttendanceRecord previous) {
        if (previous != null && !personCode.equals(previous.personCode)) {
            throw new exception.DataValidationException("previousRecord", "Must belong to the same person.");
        }
        if (checkType == CheckType.CHECK_IN) {
            if (previous != null && previous.checkType == CheckType.CHECK_IN) {
                throw invalidOrder(
                        "Already checked in at zone '" + previous.zoneId + "'. Check out before checking in again."
                );
            }
            return;
        }
        if (previous == null) {
            throw invalidOrder("Cannot check out without a prior check-in.");
        }
        if (previous.checkType == CheckType.CHECK_OUT) {
            throw invalidOrder("Already checked out. Check in before checking out again.");
        }
        if (!previous.zoneId.equalsIgnoreCase(zoneId)) {
            throw invalidOrder("Check-out zone must match the current zone '" + previous.zoneId + "'.");
        }
    }

    private exception.AttendanceOrderException invalidOrder(String reason) {
        return new exception.AttendanceOrderException(personCode, reason);
    }

    public String toCsvLine() {
        return util.CsvHelper.toCsvLine(recordId, personCode, zoneId, timestamp, checkType.name());
    }

    @Override
    public String toString() {
        return String.format(
                "AttendanceRecord: recordId=%s, personCode=%s, zoneId=%s, timestamp=%s, checkType=%s",
                recordId,
                personCode,
                zoneId,
                timestamp,
                checkType
        );
    }
}
