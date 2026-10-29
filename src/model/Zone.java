package model;

import static model.ModelValidation.*;

/**
 * NHIỆM VỤ: Thành viên 3 (Lê Tấn Thiên - SE201852)
 * MÔ TẢ:
 * - Lớp cơ sở đại diện cho các khu vực thi công / sinh hoạt trên công trường.
 * - Chịu trách nhiệm quản lý thông tin định danh khu vực (zoneId), tên gọi (zoneName).
 * - Định nghĩa cơ chế kiểm soát quyền ra/vào cổng checkAccess(Person person).
 */
public class Zone {
    private final String zoneId;
    private String zoneName;

    public Zone(String zoneId, String zoneName) {
        this.zoneId = requiredText(zoneId, "zoneId");
        this.zoneName = requiredText(zoneName, "zoneName");
    }

    public String getZoneId() { return zoneId; }
    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = requiredText(zoneName, "zoneName"); }

    public boolean checkAccess(Person person) { return person != null; }

    public String toCsvLine() {
        return util.CsvHelper.toCsvLine("REGULAR", zoneId, zoneName, "NONE", "NONE");
    }

    @Override public String toString() {
        return String.format("Zone{zoneId='%s', zoneName='%s'}", zoneId, zoneName);
    }
}
