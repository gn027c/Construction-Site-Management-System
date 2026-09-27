package controller;

import model.Incident;
import java.util.ArrayList;
import java.util.List;

/**
 * NHIỆM VỤ: Thành viên 4 (Nguyễn Tấn Lợi - SE211059)
 * MÔ TẢ:
 * - Module quản lý vòng đời hồ sơ sự cố an toàn (Incident Lifecycle): OPEN -> ASSIGNED -> RESOLVED.
 * - Tách khỏi AttendanceManager theo nguyên tắc Single Responsibility (khớp Mục 2.2 Report).
 */
public class IncidentManager {
    private final List<Incident> incidentList = new ArrayList<>();

    public boolean reportIncident(Incident incident) {
        // Sự cố mới phải bắt đầu ở trạng thái OPEN
        if (incident == null || incident.getStatus() != Incident.IncidentStatus.OPEN) {
            return false;
        }
        // Kiểm tra incidentId đã tồn tại
        if (findIncidentById(incident.getIncidentId()) != null) {
            return false;
        }
        incidentList.add(incident);
        return true;
    }

    public Incident findIncidentById(String incidentId) {
        if (incidentId == null) {
            return null;
        }
        for (Incident inc : incidentList) {
            if (inc.getIncidentId().equalsIgnoreCase(incidentId.trim())) {
                return inc;
            }
        }
        return null;
    }

    public boolean assignIncident(String incidentId, String assigneeCode) {
        if (assigneeCode == null || assigneeCode.trim().isEmpty()) {
            return false;
        }
        Incident inc = findIncidentById(incidentId);
        // Cho phép giao mới hoặc giao lại người xử lý khi sự cố chưa RESOLVED
        if (inc == null || inc.getStatus() == Incident.IncidentStatus.RESOLVED) {
            return false;
        }
        inc.setAssignedTo(assigneeCode.trim());
        inc.setStatus(Incident.IncidentStatus.ASSIGNED);
        return true;
    }

    public boolean resolveIncident(String incidentId) {
        Incident inc = findIncidentById(incidentId);
        // Chỉ đóng sự cố đã được giao người xử lý (OPEN -> ASSIGNED -> RESOLVED)
        if (inc == null || inc.getStatus() != Incident.IncidentStatus.ASSIGNED) {
            return false;
        }
        inc.setStatus(Incident.IncidentStatus.RESOLVED);
        return true;
    }

    public int purgeResolvedIncidents() {
        int before = incidentList.size();
        incidentList.removeIf(inc -> inc.getStatus() == Incident.IncidentStatus.RESOLVED);
        return before - incidentList.size();
    }

    public List<Incident> getAllIncidents() {
        return new ArrayList<>(incidentList);
    }
}
