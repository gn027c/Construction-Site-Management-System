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
        List<String> lines = util.CsvHelper.readAllLines(filePath);
        if (lines.isEmpty()) {
            return false;
        }
        incidentList.clear();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            if (i == 0 && line.toLowerCase().startsWith("incidentid")) {
                continue;
            }
            String[] parts = line.split(",", -1);
            if (parts.length >= 6) {
                String incidentId = parts[0].trim();
                String title = parts[1].trim();
                String description = parts[2].trim();
                Incident.IncidentSeverity severity;
                try {
                    severity = Incident.IncidentSeverity.valueOf(parts[3].trim().toUpperCase());
                } catch (Exception e) {
                    severity = Incident.IncidentSeverity.MEDIUM;
                }
                Incident.IncidentStatus status;
                try {
                    status = Incident.IncidentStatus.valueOf(parts[4].trim().toUpperCase());
                } catch (Exception e) {
                    status = Incident.IncidentStatus.OPEN;
                }
                String assignedTo = parts[5].trim();
                incidentList.add(new Incident(incidentId, title, description, severity, status, assignedTo));
            }
        }
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
