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
        if (incident == null || incident.getStatus() != Incident.IncidentStatus.OPEN) {
            return false;
        }
        return incidentRepository.save(incident);
    }

    public boolean reportIncident(String incidentId, String title, String description, Incident.IncidentSeverity severity) {
        if (incidentId == null || incidentId.trim().isEmpty() || title == null || title.trim().isEmpty()) {
            System.out.println("Error: Incident ID and title cannot be empty!");
            return false;
        }
        Incident incident = new Incident(incidentId.trim(), title.trim(),
                description == null ? "" : description.trim(),
                severity == null ? Incident.IncidentSeverity.LOW : severity,
                Incident.IncidentStatus.OPEN, "");
        if (reportIncident(incident)) {
            System.out.println("Incident reported with status OPEN.");
            return true;
        } else {
            System.out.println("Error: Incident ID '" + incidentId + "' already exists!");
            return false;
        }
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
            System.out.println("Error: Incident ID and Assignee Code cannot be empty!");
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
        if (personnelService != null && personnelService.findPersonByCode(assigneeCode.trim()) == null) {
            System.out.println("Error: Person with code '" + assigneeCode + "' not found in personnel registry!");
            return false;
        }

        inc.setAssignedTo(assigneeCode.trim());
        inc.setStatus(Incident.IncidentStatus.ASSIGNED);
        System.out.println("Incident '" + incidentId + "' assigned to '" + assigneeCode + "'. Status is now ASSIGNED.");
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

    public boolean resolveIncidentAndNotify(String incidentId) {
        try {
            if (resolveIncident(incidentId)) {
                System.out.println("Incident resolved.");
                return true;
            }
        } catch (exception.IncidentNotFoundException | exception.InvalidIncidentStateException e) {
            System.out.println("Incident State Error: " + e.getMessage());
        }
        return false;
    }

    public int purgeResolvedIncidents() {
        return incidentRepository.deleteResolved();
    }

    public int purgeResolvedAndNotify() {
        int count = purgeResolvedIncidents();
        System.out.println("Purged " + count + " resolved incident(s).");
        return count;
    }

    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    public void displayAllIncidents() {
        List<Incident> list = incidentRepository.findAll();
        if (list.isEmpty()) {
            System.out.println("No incidents recorded.");
            return;
        }
        for (Incident inc : list) {
            System.out.println(inc);
        }
    }
}
