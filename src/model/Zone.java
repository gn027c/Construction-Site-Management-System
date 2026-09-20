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

    // Parameterized Constructor - Yêu cầu đầy đủ thông tin bắt buộc
    public Zone(String zoneId, String zoneName) {
        if (zoneId == null || zoneId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khu vực (zoneId) không được để trống.");
        }
        if (zoneName == null || zoneName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khu vực (zoneName) không được để trống.");
        }
        this.zoneId = zoneId.trim();
        this.zoneName = zoneName.trim();
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        if (zoneId == null || zoneId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khu vực (zoneId) không được để trống.");
        }
        this.zoneId = zoneId.trim();
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        if (zoneName == null || zoneName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khu vực (zoneName) không được để trống.");
        }
        this.zoneName = zoneName.trim();
    }

    /**
     * Kiểm tra quyền truy cập cơ bản của một cá nhân vào khu vực.
     * Mặc định khu vực thông thường cho phép nhân sự hợp lệ vào.
     */
    public boolean checkAccess(Person person) {
        return person != null;
    }

    @Override
    public String toString() {
        return String.format("Zone{zoneId='%s', zoneName='%s'}", zoneId, zoneName);
    }
}
