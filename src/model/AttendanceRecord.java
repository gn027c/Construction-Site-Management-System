package model;

import exception.AppError;
import java.io.Serializable;

public class AttendanceRecord implements Serializable {
    public enum CheckType {
        CHECK_IN,
        CHECK_OUT
    }

    private final String recordId;
    private final String personCode;
    private final String zoneId;
    private final String timestamp;
    private final CheckType checkType;

    public AttendanceRecord(String recordId, String personCode, String zoneId, String timestamp, CheckType checkType) {
        this.recordId = util.ValidationHelper.requireText(recordId, "recordId", "Must not be empty.");
        this.personCode = util.ValidationHelper.requireText(personCode, "personCode", "Must not be empty.");
        this.zoneId = util.ValidationHelper.requireText(zoneId, "zoneId", "Must not be empty.");
        this.timestamp = util.ValidationHelper.requireText(timestamp, "timestamp", "Must not be empty.");
        this.checkType = util.ValidationHelper.requireNotNull(checkType, "checkType", "Must not be null.");
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

    // Kiểm tra quy trình vào/ra hợp lệ:
    // - Không được Check-in 2 lần liên tiếp khi chưa Check-out.
    // - Phải Check-out đúng khu vực (zone) đã Check-in trước đó.
    public void assertCanFollow(AttendanceRecord lastRecord) {
        if (checkType == CheckType.CHECK_IN) {
            if (lastRecord != null && lastRecord.getCheckType() == CheckType.CHECK_IN) {
                throw new AppError("Attendance FSM Violation for Person [" + personCode
                        + "]: Already CHECKED_IN at zone '" + lastRecord.getZoneId()
                        + "'. Cannot CHECK_IN twice sequentially.");
            }
        } else if (checkType == CheckType.CHECK_OUT) {
            if (lastRecord == null) {
                throw new AppError("Attendance FSM Violation for Person [" + personCode
                        + "]: Cannot CHECK_OUT without prior CHECK_IN record.");
            }
            if (lastRecord.getCheckType() == CheckType.CHECK_OUT) {
                throw new AppError("Attendance FSM Violation for Person [" + personCode
                        + "]: Already CHECKED_OUT. Cannot CHECK_OUT again without CHECK_IN.");
            }
            if (!Zone.isSameId(lastRecord.getZoneId(), zoneId)) {
                throw new AppError("Attendance FSM Violation for Person [" + personCode
                        + "]: Zone mismatch on CHECK_OUT! Currently inside zone '" + lastRecord.getZoneId()
                        + "', but attempting to CHECK_OUT at zone '" + zoneId + "'.");
            }
        }
    }

    public String toCsvLine() {
        return util.CsvHelper.toCsvLine(recordId, personCode, zoneId, timestamp, checkType.name());
    }

    @Override
    public String toString() {
        return String.format("AttendanceRecord: recordId=%s, personCode=%s, zoneId=%s, timestamp=%s, checkType=%s",
                recordId, personCode, zoneId, timestamp, checkType);
    }
}
