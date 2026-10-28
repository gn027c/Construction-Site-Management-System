package service;

import model.Incident;
import repository.IncidentRepository;
import java.util.List;

/**
 * TẦNG NGHIỆP VỤ (SERVICE LAYER)
 * PHỤ TRÁCH: Thành viên 4 (Nguyễn Tấn Lợi - SE211059)
 * MÔ TẢ: Xử lý vòng đời sự cố an toàn (OPEN -> ASSIGNED -> RESOLVED) và phân công nhân sự.
 */
public class IncidentService {
    private final IncidentRepository incidentRepository;
    private final PersonnelService personnelService;

    public IncidentService(PersonnelService personnelService) {
        this(new IncidentRepository(), personnelService);
    }

    public IncidentService(IncidentRepository incidentRepository, PersonnelService personnelService) {
        this.incidentRepository = (incidentRepository != null) ? incidentRepository : new IncidentRepository();
        this.personnelService = personnelService;
    }

    public boolean reportIncident(Incident incident) {
        if (incident == null || incident.getStatus() != Incident.IncidentStatus.OPEN || !incident.getAssignedTo().isEmpty()) {
            return false;
        }
        return incidentRepository.save(incident);
    }

    public boolean reportIncident(String incidentId, String title, String description, Incident.IncidentSeverity severity) {
        if (incidentId == null || incidentId.trim().isEmpty() || title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {

            return false;
        }
        Incident incident = new Incident(incidentId.trim(), title.trim(),
                description == null ? "" : description.trim(),
                severity == null ? Incident.IncidentSeverity.LOW : severity,
                Incident.IncidentStatus.OPEN, "");
        return reportIncident(incident);
    }

    public PersonnelService getPersonnelService() {
        return personnelService;
    }

    public Incident findIncidentById(String incidentId) {
        return incidentRepository.findById(incidentId);
    }

    public boolean assignIncident(String incidentId, String assigneeCode)
            throws exception.IncidentNotFoundException, exception.InvalidIncidentStateException {
        if (incidentId == null || incidentId.trim().isEmpty() || assigneeCode == null || assigneeCode.trim().isEmpty()) {

            return false;
        }

        Incident inc = findIncidentById(incidentId.trim());
        if (inc == null) {
            throw new exception.IncidentNotFoundException(incidentId.trim());
        }

        if (inc.getStatus() != Incident.IncidentStatus.OPEN) {
            throw new exception.InvalidIncidentStateException(incidentId.trim(),
                    inc.getStatus().name(), "Only OPEN incidents can be assigned to personnel.");
        }

        // Xác thực nhân sự phụ trách tồn tại qua personnelService
        if (personnelService == null) return false;
        try {
            personnelService.findPersonByCode(assigneeCode.trim());
        } catch (exception.PersonNotFoundException e) {
            return false;
        }

        inc.setAssignedTo(assigneeCode.trim());
        inc.setStatus(Incident.IncidentStatus.ASSIGNED);

        return true;
    }

    public boolean resolveIncident(String incidentId)
            throws exception.IncidentNotFoundException, exception.InvalidIncidentStateException {
        if (incidentId == null || incidentId.trim().isEmpty()) {
            return false;
        }

        Incident inc = findIncidentById(incidentId.trim());
        if (inc == null) {
            throw new exception.IncidentNotFoundException(incidentId.trim());
        }

        if (inc.getStatus() != Incident.IncidentStatus.ASSIGNED) {
            throw new exception.InvalidIncidentStateException(incidentId.trim(),
                    inc.getStatus().name(), "Only ASSIGNED incidents can be transitioned to RESOLVED.");
        }

        inc.setStatus(Incident.IncidentStatus.RESOLVED);
        return true;
    }

    public int purgeResolvedIncidents() {
        return incidentRepository.deleteResolved();
    }

    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    public boolean loadFromCsv() {
        return loadFromCsv("data/incidents.csv");
    }

    public boolean loadFromCsv(String filePath) {
        return incidentRepository.loadFromCsv(filePath);
    }

    public boolean saveToCsv() {
        return saveToCsv("data/incidents.csv");
    }

    public boolean saveToCsv(String filePath) {
        return incidentRepository.saveToCsv(filePath);
    }
}
