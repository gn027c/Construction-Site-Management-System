package model;

/**
 * NHIỆM VỤ: Thành viên 4 (Nguyễn Tấn Lợi - SE211059)
 * MÔ TẢ:
 * - Lớp thực thể đại diện cho hồ sơ sự cố an toàn lao động phát sinh tại hiện trường.
 * - Chịu trách nhiệm quản lý thông tin sự cố, mức độ nguy hiểm, trạng thái xử lý và nhân sự liên quan.
 * - Cung cấp các hành vi phân công xử lý và cập nhật tiến độ giải quyết sự cố.
 */
public class Incident {

    public enum IncidentSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum IncidentStatus {
        OPEN,
        ASSIGNED,
        RESOLVED
    }

    private String incidentId;
    private String title;
    private String description;
    private IncidentSeverity severity;
    private IncidentStatus status;
    private String assignedTo;

    public Incident(String incidentId, String title, String description,
                    IncidentSeverity severity, IncidentStatus status, String assignedTo) {
        if (incidentId == null || incidentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sự cố (incidentId) không được để trống.");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề sự cố không được để trống.");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Mô tả sự cố không được để trống.");
        }
        if (severity == null) {
            throw new IllegalArgumentException("Mức độ nghiêm trọng không được để trống.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái sự cố không được để trống.");
        }
        this.incidentId = incidentId.trim();
        this.title = title.trim();
        this.description = description.trim();
        this.severity = severity;
        this.status = status;
        this.assignedTo = (assignedTo != null) ? assignedTo.trim() : "";
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        if (incidentId == null || incidentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sự cố không được để trống.");
        }
        this.incidentId = incidentId.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề sự cố không được để trống.");
        }
        this.title = title.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Mô tả sự cố không được để trống.");
        }
        this.description = description.trim();
    }

    public IncidentSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(IncidentSeverity severity) {
        if (severity == null) {
            throw new IllegalArgumentException("Mức độ nghiêm trọng không được để trống.");
        }
        this.severity = severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Trạng thái sự cố không được để trống.");
        }
        this.status = status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = (assignedTo != null) ? assignedTo.trim() : "";
    }

    @Override
    public String toString() {
        return String.format("Incident{incidentId='%s', title='%s', severity=%s, status=%s, assignedTo='%s'}",
                incidentId, title, severity, status, assignedTo.isEmpty() ? "None" : assignedTo);
    }
}
