package model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Loại quẹt thẻ điểm danh: Vào (CHECK_IN) hoặc Ra (CHECK_OUT).
 */
enum RecordType implements Serializable {
    CHECK_IN("Vào ca"),
    CHECK_OUT("Ra ca");

    private final String description;

    RecordType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

/**
 * Lớp đại diện cho một bản ghi điểm danh vào/ra công trường.
 * @author Nguyễn Tấn Lợi - SE211059
 */
public class AttendanceRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String recordId;          // Mã bản ghi duy nhất (VD: REC-1001)
    private String personCode;        // Mã quẹt thẻ của nhân sự
    private String zoneId;            // Mã khu vực quẹt thẻ
    private LocalDateTime timestamp;  // Thời điểm chính xác
    private RecordType type;          // CHECK_IN hoặc CHECK_OUT

    public AttendanceRecord() {
        this.timestamp = LocalDateTime.now();
    }

    public AttendanceRecord(String recordId, String personCode, String zoneId,
                            LocalDateTime timestamp, RecordType type) {
        setRecordId(recordId);
        setPersonCode(personCode);
        setZoneId(zoneId);
        this.timestamp = (timestamp != null) ? timestamp : LocalDateTime.now();
        this.type = (type != null) ? type : RecordType.CHECK_IN;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = (recordId != null) ? recordId.trim().toUpperCase() : "";
    }

    public String getPersonCode() {
        return personCode;
    }

    public void setPersonCode(String personCode) {
        this.personCode = (personCode != null) ? personCode.trim().toUpperCase() : "";
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = (zoneId != null) ? zoneId.trim().toUpperCase() : "";
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        if (timestamp != null) this.timestamp = timestamp;
    }

    public RecordType getType() {
        return type;
    }

    public void setType(RecordType type) {
        if (type != null) this.type = type;
    }

    @Override
    public String toString() {
        return String.format("[%s] Nhân sự: %-8s | Khu vực: %-8s | Thời gian: %s | Loại: %s",
                recordId, personCode, zoneId, timestamp, type.getDescription());
    }
}
