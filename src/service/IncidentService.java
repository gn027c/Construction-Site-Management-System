package service;

import model.Incident;
import repository.IncidentRepository;
import java.util.List;

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

    public boolean reportIncident(String incidentId, String title, String description,
                                  Incident.IncidentSeverity severity) throws exception.AppError {
        Incident incident = new Incident(incidentId, title, description, severity, Incident.IncidentStatus.OPEN, "");
        if (!incidentRepository.save(incident)) {
            throw new exception.AppError("Incident ID already exists: " + incident.getIncidentId());
        }
        return true;
    }

    public PersonnelService getPersonnelService() {
        return personnelService;
    }

    public Incident findIncidentById(String incidentId) {
        return incidentRepository.findById(incidentId);
    }

    public boolean assignIncident(String incidentId, String assigneeCode)
            throws exception.NotFoundException, exception.AppError {
        Incident incident = incidentRepository.findByIdOrThrow(incidentId);
        incident.assignTo(personnelService.findPersonByCode(assigneeCode));
        return true;
    }

    public boolean resolveIncident(String incidentId)
            throws exception.NotFoundException, exception.AppError {
        incidentRepository.findByIdOrThrow(incidentId).resolve();
        return true;
    }

    public boolean isAssignedTo(String personCode) {
        for (Incident incident : incidentRepository.findAll()) {
            if (incident.getAssignedTo().equalsIgnoreCase(personCode)) {
                return true;
            }
        }
        return false;
    }

    public int purgeResolvedIncidents() {
        return incidentRepository.deleteResolved();
    }

    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }

    public boolean loadFromCsv(String filePath) {
        return incidentRepository.loadFromCsv(filePath);
    }

    public boolean saveToCsv(String filePath) {
        return incidentRepository.saveToCsv(filePath);
    }
}
