package model;

import java.io.Serializable;

/**
 * Vai trò phân quyền trên công trường (Role-Based Access Control).
 * @author Huỳnh Nguyễn Hoàng Khang - SE201461
 */
public enum Role implements Serializable {
    WORKER("Công nhân"),
    CONTRACTOR("Nhà thầu"),
    VISITOR("Khách tham quan"),
    SAFETY_OFFICER("Cán bộ An toàn"),
    SITE_MANAGER("Chỉ huy trưởng");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
