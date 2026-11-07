package repository;

import model.Incident;
import java.util.ArrayList;
import java.util.List;

public class IncidentRepository {
    private static final String LEGACY_HEADER = "incidentId,title,description,severity,status,assignedTo";
    private static final String HEADER = LEGACY_HEADER + ",reportedAt";
    private static final int LEGACY_COLUMN_COUNT = 6;
    private static final int COLUMN_COUNT = 7;
    private static final java.time.format.DateTimeFormatter REPORT_TIME = util.DateHelper.TIMESTAMP_FORMATTER;

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
            if (util.IdHelper.isSame(inc.getIncidentId(), incidentId)) {
                return inc;
            }
        }
        return null;
    }

    public Incident findByIdOrThrow(String incidentId) throws exception.NotFoundException {
        Incident incident = findById(incidentId);
        if (incident == null) {
            throw new exception.NotFoundException("Incident record", "ID", String.valueOf(incidentId).trim());
        }
        return incident;
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
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            return false;
        }
        List<String> lines = util.CsvHelper.readAllLines(filePath);
        if (lines.isEmpty()) {
            throw new exception.DataValidationException(file.getName() + " line 1: missing header.");
        }
        String fileHeader = lines.get(0).trim();
        boolean legacy = LEGACY_HEADER.equals(fileHeader);
        if (!legacy && !HEADER.equals(fileHeader)) {
            throw new exception.DataValidationException(
                    file.getName() + " line 1: unexpected header. Expected: " + HEADER);
        }
        List<Incident> incidents = new ArrayList<>();
        java.util.Set<String> keys = new java.util.HashSet<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                List<String> columns = util.CsvHelper.parseLine(line);
                int expected = legacy ? LEGACY_COLUMN_COUNT : COLUMN_COUNT;
                if (columns.size() != expected) {
                    throw new exception.DataValidationException(
                            "Expected " + expected + " columns, found " + columns.size() + ".");
                }
                Incident incident = parse(columns, legacy);
                if (!keys.add(incident.getIncidentId().toUpperCase())) {
                    throw new exception.DataValidationException("Duplicate identifier " + incident.getIncidentId() + ".");
                }
                incidents.add(incident);
            } catch (RuntimeException e) {
                throw new exception.DataValidationException(
                        file.getName() + " line " + (i + 1) + ": " + e.getMessage());
            }
        }
        incidentList.clear();
        incidentList.addAll(incidents);
        return true;
    }

    private Incident parse(List<String> columns, boolean legacy) {
        java.time.LocalDateTime reportedAt = null;
        if (!legacy && !columns.get(6).trim().isEmpty()) {
            reportedAt = java.time.LocalDateTime.parse(columns.get(6).trim(), REPORT_TIME);
        }
        return new Incident(
                columns.get(0).trim(), columns.get(1).trim(), columns.get(2).trim(),
                Incident.IncidentSeverity.valueOf(columns.get(3).trim()),
                Incident.IncidentStatus.valueOf(columns.get(4).trim()),
                columns.get(5).trim(), reportedAt);
    }

    public boolean saveToCsv(String filePath) {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Incident inc : incidentList) {
            lines.add(inc.toCsvLine());
        }
        return util.CsvHelper.writeAllLines(filePath, lines);
    }

}
