package model;

import static model.ModelValidation.*;
import java.io.Serializable;

/**
 * NHIỆM VỤ: Thành viên 4 (Nguyễn Tấn Lợi - SE211059)
 * MÔ TẢ:
 * - Lớp đại diện cho một bản ghi điểm danh vào/ra (Check-in / Check-out) tại công trường.
 * - Chịu trách nhiệm quản lý mã bản ghi (recordId), mã nhân sự (personCode), mã khu vực (zoneId),
 *   thời điểm quẹt thẻ (timestamp) và loại thao tác (CHECK_IN / CHECK_OUT).
 * - Giữ cố định định danh/loại thao tác và kiểm tra chu kỳ qua assertCanFollow().
 */
public class AttendanceRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    public enum CheckType { CHECK_IN, CHECK_OUT }

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

    public String getRecordId() { return recordId; }
    public String getPersonCode() { return personCode; }
    public String getZoneId() { return zoneId; }
    public String getTimestamp() { return timestamp; }
    public CheckType getCheckType() { return checkType; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp(timestamp); }

    /** Validate a new swipe against the preceding record of the same person. */
    public void assertCanFollow(AttendanceRecord previous) {
        if (previous != null && !personCode.equals(previous.personCode))
            throw new exception.DataValidationException("previousRecord", "Must belong to the same person.");
        if (checkType == CheckType.CHECK_IN) {
            if (previous != null && previous.checkType == CheckType.CHECK_IN)
                throw invalidOrder("Already checked in at zone '" + previous.zoneId + "'. Check out before checking in again.");
            return;
        }
        if (previous == null) throw invalidOrder("Cannot check out without a prior check-in.");
        if (previous.checkType == CheckType.CHECK_OUT) throw invalidOrder("Already checked out. Check in before checking out again.");
        if (!previous.zoneId.equalsIgnoreCase(zoneId))
            throw invalidOrder("Check-out zone must match the current zone '" + previous.zoneId + "'.");
    }

    private exception.AttendanceOrderException invalidOrder(String reason) {
        return new exception.AttendanceOrderException(personCode, reason);
    }

    public String toCsvLine() {
        return util.CsvHelper.toCsvLine(recordId, personCode, zoneId, timestamp, checkType.name());
    }

    @Override public String toString() {
        return String.format("AttendanceRecord: recordId=%s, personCode=%s, zoneId=%s, timestamp=%s, checkType=%s",
                recordId, personCode, zoneId, timestamp, checkType);
    }
}
