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

    public boolean loadFromCsv(String filePath) {
        IncidentRepository staged = new IncidentRepository();
        try {
            List<String[]> rows = util.CsvHelper.readRows(filePath);
            if (rows.isEmpty() || !rows.get(0)[0].equals("incidentId")) return false;
            for (int i = 1; i < rows.size(); i++) {
                String[] p = rows.get(i);
                if (util.CsvHelper.isBlankRow(p)) continue;
                if (p.length != 6) return false;
                Incident.IncidentStatus status = Incident.IncidentStatus.valueOf(p[4]);
                if ((status == Incident.IncidentStatus.OPEN) != p[5].trim().isEmpty()) return false;
                if (!staged.save(new Incident(p[0], p[1], p[2], Incident.IncidentSeverity.valueOf(p[3]), status, p[5]))) return false;
            }
        } catch (RuntimeException e) { return false; }
        incidentList.clear(); incidentList.addAll(staged.incidentList);
        return true;
    }

    public boolean saveToCsv(String filePath) {
        List<String> lines = new ArrayList<>();
        lines.add("incidentId,title,description,severity,status,assignedTo");
        for (Incident inc : incidentList) {
            lines.add(inc.toCsvLine());
        }
        return util.CsvHelper.writeAllLines(filePath, lines);
    }
}
