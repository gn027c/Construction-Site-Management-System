package service;

import model.Incident;
import repository.IncidentRepository;
import java.util.List;

/**
 * Coordinates incident storage and personnel lookup for incident operations.
 *
 * <p>The service confirms that an assignee exists before asking the incident to
 * change its lifecycle status. The incident model owns transition validation.
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
        if (incident == null
                || incident.getStatus() != Incident.IncidentStatus.OPEN
                || !incident.getAssignedTo().isEmpty()) {
            return false;
        }
        return incidentRepository.save(incident);
    }

    public boolean reportIncident(
            String incidentId,
            String title,
            String description,
            Incident.IncidentSeverity severity
    ) {
        return reportIncident(Incident.report(incidentId, title, description, severity));
    }

    public PersonnelService getPersonnelService() {
        return personnelService;
    }

    public Incident findIncidentById(String incidentId) {
        return incidentRepository.findById(incidentId);
    }

    /**
     * Confirms the assignee exists before invoking the incident's assignment operation.
     */
    public boolean assignIncident(String incidentId, String assigneeCode)
            throws exception.IncidentNotFoundException, exception.InvalidIncidentStateException {
        if (incidentId == null
                || incidentId.trim().isEmpty()
                || assigneeCode == null
                || assigneeCode.trim().isEmpty()) {
            return false;
        }
        Incident inc = findIncidentById(incidentId.trim());
        if (inc == null) {
            throw new exception.IncidentNotFoundException(incidentId.trim());
        }
        if (personnelService == null) {
            return false;
        }
        try {
            personnelService.findPersonByCode(assigneeCode.trim());
        } catch (exception.PersonNotFoundException e) {
            return false;
        }
        inc.assignTo(assigneeCode);
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
        inc.resolve();
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
