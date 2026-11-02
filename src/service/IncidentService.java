package service;

import model.incident.Incident;
import repository.IncidentRepository;
import java.util.List;
import java.util.ArrayList;

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

    public boolean reportIncident(
            String incidentId,
            String title,
            String description,
            Incident.IncidentSeverity severity,
            String zoneId,
            String reportedBy
    ) {
        return reportIncident(Incident.report(incidentId, title, description, severity, zoneId, reportedBy));
    }

    public PersonnelService getPersonnelService() {
        return personnelService;
    }

    public Incident findIncidentById(String incidentId) {
        return incidentRepository.findById(incidentId);
    }

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
        model.person.Person assignee;
        try {
            assignee = personnelService.findPersonByCode(assigneeCode.trim());
        } catch (exception.PersonNotFoundException e) {
            return false;
        }
        if (!assignee.canHandleIncidents()) {
            throw new exception.DataValidationException(
                    "assignedTo", assignee.getCode() + " cannot handle incidents; choose active site personnel."
            );
        }
        inc.assignTo(assigneeCode);
        return true;
    }

    public boolean resolveIncident(String incidentId, String note)
            throws exception.IncidentNotFoundException, exception.InvalidIncidentStateException {
        if (incidentId == null || incidentId.trim().isEmpty()) {
            return false;
        }
        Incident inc = findIncidentById(incidentId.trim());
        if (inc == null) {
            throw new exception.IncidentNotFoundException(incidentId.trim());
        }
        inc.resolve(note);
        return true;
    }

    public int purgeResolvedIncidents() {
        return archiveResolvedIncidents();
    }

    public int archiveResolvedIncidents() {
        int archived = 0;
        for (Incident incident : incidentRepository.findAll()) {
            if (incident.isResolved() && incident.archive()) {
                archived++;
            }
        }
        return archived;
    }

    public List<Incident> getActiveIncidents() {
        List<Incident> active = new ArrayList<>();
        for (Incident incident : incidentRepository.findAll()) {
            if (!incident.isArchived()) {
                active.add(incident);
            }
        }
        return active;
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
