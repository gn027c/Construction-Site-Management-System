package model;

/**
 * NHIỆM VỤ: Thành viên 3 (Lê Tấn Thiên - SE201852)
 * MÔ TẢ:
 * - Lớp cơ sở đại diện cho các khu vực thi công / sinh hoạt trên công trường.
 * - Chịu trách nhiệm quản lý thông tin định danh khu vực (zoneId), tên gọi (zoneName).
 * - Định nghĩa cơ chế kiểm soát quyền ra/vào cổng checkAccess(Person person).
 */

public class Zone {
    protected String zoneId;
    protected String zoneName;

    public Zone(String zoneId, String zoneName) {
        if (zoneId == null || zoneId.trim().isEmpty()) {
            throw new IllegalArgumentException("Zone ID cannot be empty.");
        }
        if (zoneName == null || zoneName.trim().isEmpty()) {
            throw new IllegalArgumentException("Zone name cannot be empty.");
        }
        this.zoneId = zoneId.trim();
        this.zoneName = zoneName.trim();
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        if (zoneId == null || zoneId.trim().isEmpty()) {
            throw new IllegalArgumentException("Zone ID cannot be empty.");
        }
        this.zoneId = zoneId.trim();
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        if (zoneName == null || zoneName.trim().isEmpty()) {
            throw new IllegalArgumentException("Zone name cannot be empty.");
        }
        this.zoneName = zoneName.trim();
    }

    // Regular zones allow access by default if person is not null
    public boolean checkAccess(Person person) {
        return person != null;
    }

    public String toCsvLine() {
        return String.format("REGULAR,%s,%s,NONE,NONE", zoneId, zoneName);
    }

    @Override
    public String toString() {
        return String.format("Zone{zoneId='%s', zoneName='%s'}", zoneId, zoneName);
    }
}
