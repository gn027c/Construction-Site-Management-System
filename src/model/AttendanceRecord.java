package model;

import java.io.Serializable;

/**
 * NHIỆM VỤ: Thành viên 4 (Nguyễn Tấn Lợi - SE211059)
 * MÔ TẢ:
 * - Lớp đại diện cho một bản ghi điểm danh vào/ra (Check-in / Check-out) tại công trường.
 * - Chịu trách nhiệm quản lý mã bản ghi (recordId), mã nhân sự (personCode), mã khu vực (zoneId),
 *   thời điểm quẹt thẻ (timestamp) và loại thao tác (CHECK_IN / CHECK_OUT).
 */
public class AttendanceRecord implements Serializable {
    public enum CheckType {
        CHECK_IN,
        CHECK_OUT
    }

    private String recordId;
    private String personCode;
    private String zoneId;
    private String timestamp;
    private CheckType checkType;

    public AttendanceRecord(String recordId, String personCode, String zoneId, String timestamp, CheckType checkType) {
        this.recordId = recordId;
        this.personCode = personCode;
        this.zoneId = zoneId;
        this.timestamp = timestamp;
        this.checkType = checkType;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getPersonCode() {
        return personCode;
    }

    public void setPersonCode(String personCode) {
        this.personCode = personCode;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public CheckType getCheckType() {
        return checkType;
    }

    public void setCheckType(CheckType checkType) {
        this.checkType = checkType;
    }

    @Override
    public String toString() {
        return String.format("AttendanceRecord: recordId=%s, personCode=%s, zoneId=%s, timestamp=%s, checkType=%s",
                recordId, personCode, zoneId, timestamp, checkType);
    }
}
