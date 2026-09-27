package controller;

import model.Incident;
import java.util.ArrayList;
import java.util.List;

/**
 * TASK: Member 3 (Le Tan Thien - SE201382)
 * DESCRIPTION:
 * - Manages safety incident reports.
 * - Handles incident logging, assigning responsibility, and updating resolution status.
 */
public class IncidentManager {
    private List<Incident> incidentList;

    public IncidentManager() {
        this.incidentList = new ArrayList<>();
    }

    public List<Incident> getIncidentList() {
        return incidentList;
    }

    // Log a new incident (Check for duplicate incidentId)
    public boolean logIncident(Incident incident) {
        if (incident == null || incident.getIncidentId() == null || incident.getIncidentId().isEmpty()) {
            System.out.println("Error: Invalid incident data!");
            return false;
        }
        if (findIncidentById(incident.getIncidentId()) != null) {
            System.out.println("Error: Incident ID '" + incident.getIncidentId() + "' already exists!");
            return false;
        }
        incidentList.add(incident);
        System.out.println("Successfully reported incident: " + incident.getTitle());
        return true;
    }

    // Find an incident by its ID
    public Incident findIncidentById(String incidentId) {
        if (incidentId == null || incidentId.isEmpty()) return null;
        for (Incident inc : incidentList) {
            if (inc.getIncidentId().equalsIgnoreCase(incidentId)) {
                return inc;
            }
        }
        return null;
    }

    // Assign an incident to a person
    public boolean assignIncident(String incidentId, String assigneeCode) {
        Incident inc = findIncidentById(incidentId);
        if (inc == null) {
            System.out.println("Error: Incident ID '" + incidentId + "' not found!");
            return false;
        }
        inc.setAssigneeCode(assigneeCode);
        inc.setStatus("IN_PROGRESS");
        System.out.println("Assigned incident '" + incidentId + "' to assignee code: " + assigneeCode);
        return true;
    }

    // Update status of an incident
    public boolean updateIncidentStatus(String incidentId, String newStatus) {
        Incident inc = findIncidentById(incidentId);
        if (inc == null) {
            System.out.println("Error: Incident ID '" + incidentId + "' not found!");
            return false;
        }
        inc.setStatus(newStatus);
        System.out.println("Updated status of incident '" + incidentId + "' to: " + newStatus);
        return true;
    }
}
