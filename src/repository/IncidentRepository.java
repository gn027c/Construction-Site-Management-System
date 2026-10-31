package repository;

import model.incident.Incident;
import java.util.ArrayList;
import java.util.List;

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
        String normalized = util.IdentifierHelper.normalizeForLookup(incidentId);
        if (normalized == null) {
            return null;
        }
        for (Incident inc : incidentList) {
            if (inc.getIncidentId().equals(normalized)) {
                return inc;
            }
        }
        return null;
    }

    public int deleteResolved() {
        return archiveResolved();
    }

    public int archiveResolved() {
        int count = 0;
        for (Incident incident : incidentList) {
            if (incident.isResolved() && incident.archive()) {
                count++;
            }
        }
        return count;
    }

    public List<Incident> findAll() {
        return new ArrayList<>(incidentList);
    }

    public boolean loadFromCsv(String filePath) {
        IncidentRepository staged = new IncidentRepository();
        try {
            List<String[]> rows = util.CsvHelper.readRows(filePath);
            if (rows.isEmpty() || !java.util.Arrays.equals(rows.get(0), new String[] {
                    "incidentId", "title", "description", "severity", "status", "assignedTo",
                    "reportedAt", "resolvedAt", "archived"
            })) {
                return false;
            }
            for (int i = 1; i < rows.size(); i++) {
                String[] p = rows.get(i);
                if (util.CsvHelper.isBlankRow(p)) {
                    continue;
                }
                if (p.length != 9 || !(p[8].equals("true") || p[8].equals("false"))) {
                    return false;
                }
                Incident.IncidentStatus status = Incident.IncidentStatus.valueOf(p[4]);
                if (
                        !staged.save(
                                new Incident(
                                        p[0], p[1], p[2], Incident.IncidentSeverity.valueOf(p[3]), status,
                                        p[5], p[6], p[7], Boolean.parseBoolean(p[8])
                                )
                        )
                ) {
                    return false;
                }
            }
        } catch (RuntimeException e) {
            return false;
        }
        incidentList.clear();
        incidentList.addAll(staged.incidentList);
        return true;
    }

    public boolean saveToCsv(String filePath) {
        List<String> lines = new ArrayList<>();
        lines.add("incidentId,title,description,severity,status,assignedTo,reportedAt,resolvedAt,archived");
        for (Incident inc : incidentList) {
            lines.add(inc.toCsvLine());
        }
        return util.CsvHelper.writeAllLines(filePath, lines);
    }
}
