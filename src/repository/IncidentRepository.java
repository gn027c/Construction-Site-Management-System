package repository;

import model.Incident;
import java.util.ArrayList;
import java.util.List;

/**
 * TẦNG LƯU TRỮ (REPOSITORY LAYER)
 * PHỤ TRÁCH: Thành viên 4 (Nguyễn Tấn Lợi - SE211059)
 * MÔ TẢ: Quản lý lưu trữ và truy xuất tập hợp hồ sơ sự cố an toàn (Incident).
 */
public class IncidentRepository {
    private final List<Incident> incidentList = new ArrayList<>();

    public boolean save(Incident incident) {
        if (incident == null || incident.getIncidentId() == null) {
            return false;
        }
        if (findById(incident.getIncidentId()) != null) {
            return false;
        }
        return incidentList.add(incident);
    }

    public Incident findById(String incidentId) {
        if (incidentId == null || incidentId.trim().isEmpty()) {
            return null;
        }
        for (Incident inc : incidentList) {
            if (inc.getIncidentId().equalsIgnoreCase(incidentId.trim())) {
                return inc;
            }
        }
        return null;
    }

    public int deleteResolved() {
        int before = incidentList.size();
        incidentList.removeIf(inc -> inc.getStatus() == Incident.IncidentStatus.RESOLVED);
        return before - incidentList.size();
    }

    public List<Incident> findAll() {
        return new ArrayList<>(incidentList);
    }
}
