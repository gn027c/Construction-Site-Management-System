package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import model.Incident;
import model.Permission;
import service.AuthenticationService;
import service.IncidentService;
import util.InputHelper;
import view.ConsoleUi;
import view.Pagination;

public class IncidentController {
    private final IncidentService incidentService;
    private final AuthenticationService authentication;

    public IncidentController(IncidentService incidentService, AuthenticationService authentication) {
        this.incidentService = incidentService;
        this.authentication = authentication;
    }

    public void handleMenu(Scanner scanner) {
        String filter = "";
        String statusMessage = null;
        List<Incident> incidents = fetchIncidents();
        List<Incident> filtered = filterIncidents(incidents, filter);
        Pagination<Incident> pagination = new Pagination<>(filtered, 8);
        boolean back = false;

        while (!back) {
            ConsoleUi.clearScreen();
            String breadcrumb = "INCIDENT MANAGEMENT | PAGE "
                    + (pagination.isEmpty() ? "1/1" : (pagination.pageNumber() + "/" + pagination.pageCount()));
            if (!filter.isEmpty()) {
                breadcrumb += " | Filter: \"" + filter + "\"";
            }
            ConsoleUi.breadcrumb(breadcrumb);

            showIncidentsTable(pagination.currentPage());

            if (statusMessage != null) {
                System.out.println(statusMessage);
                statusMessage = null;
            }

            System.out.println("Actions: 1 (Report), 2 (Assign), 3 (Resolve), 4 (Purge), 0 (Back)");
            String command = InputHelper.getString(
                    scanner, "Select [F (Filter), C (Clear filter), N (Next), P (Prev), L (Last)]: ");

            if (command.equalsIgnoreCase("1")) {
                authentication.require(Permission.REPORT_INCIDENTS);
                statusMessage = reportIncidentFlow(scanner);
                incidents = fetchIncidents();
                filtered = filterIncidents(incidents, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("2")) {
                authentication.require(Permission.ASSIGN_INCIDENTS);
                statusMessage = assignIncidentFlow(scanner);
                incidents = fetchIncidents();
                filtered = filterIncidents(incidents, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("3")) {
                authentication.require(Permission.RESOLVE_INCIDENTS);
                statusMessage = resolveIncidentFlow(scanner);
                incidents = fetchIncidents();
                filtered = filterIncidents(incidents, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("4")) {
                authentication.require(Permission.PURGE_INCIDENTS);
                int count = incidentService.purgeResolvedIncidents();
                statusMessage = "[OK] Purged " + count + " resolved incident(s).";
                incidents = fetchIncidents();
                filtered = filterIncidents(incidents, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("F")) {
                String term = InputHelper.getString(scanner, "Filter by ID, Title, Severity, Status, or Assignee: ");
                filter = term.trim().toLowerCase();
                filtered = filterIncidents(incidents, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("C")) {
                filter = "";
                filtered = filterIncidents(incidents, filter);
                pagination = new Pagination<>(filtered, 8);
            } else if (command.equalsIgnoreCase("N")) {
                pagination.next();
            } else if (command.equalsIgnoreCase("P")) {
                pagination.previous();
            } else if (command.equalsIgnoreCase("L")) {
                pagination.last();
            } else if (command.equals("0")) {
                back = true;
            } else {
                System.out.println("[WARN] Invalid command. Choose 1, 2, 3, 4, F, C, N, P, L or 0.");
            }
        }
    }

    private List<Incident> fetchIncidents() {
        if (authentication.allows(Permission.VIEW_INCIDENTS)) {
            return incidentService.getAllIncidents();
        }
        return Collections.emptyList();
    }

    private List<Incident> filterIncidents(List<Incident> list, String filter) {
        if (filter == null || filter.trim().isEmpty()) {
            return new ArrayList<>(list);
        }
        String term = filter.trim().toLowerCase();
        List<Incident> matches = new ArrayList<>();
        for (Incident inc : list) {
            boolean match = inc.getIncidentId().toLowerCase().contains(term)
                    || inc.getTitle().toLowerCase().contains(term)
                    || inc.getSeverity().name().toLowerCase().contains(term)
                    || inc.getStatus().name().toLowerCase().contains(term)
                    || inc.getAssignedTo().toLowerCase().contains(term);
            if (match) {
                matches.add(inc);
            }
        }
        return matches;
    }

    private void showIncidentsTable(List<Incident> list) {
        if (list == null || list.isEmpty()) {
            System.out.println("No incidents recorded.");
            return;
        }
        String[] headers = { "ID", "SEVERITY", "STATUS", "ASSIGNED", "TITLE" };
        List<String[]> rows = new ArrayList<>();
        for (Incident incident : list) {
            rows.add(new String[] {
                    incident.getIncidentId(),
                    incident.getSeverity().name(),
                    incident.getStatus().name(),
                    incident.getAssignedTo().isEmpty() ? "-" : incident.getAssignedTo(),
                    incident.getTitle()
            });
        }
        ConsoleUi.table(headers, rows);
    }

    private String reportIncidentFlow(Scanner scanner) {
        System.out.println("\n--- REPORT NEW INCIDENT ---");
        String id = InputHelper.getString(scanner, "Enter Incident ID (e.g., INC001, 0 to cancel): ");
        if (id.equals("0")) {
            return null;
        }
        String title = InputHelper.getString(scanner, "Enter Incident Title: ");
        String description = InputHelper.getString(scanner, "Enter Incident Description: ");

        ConsoleUi.menu("SELECT SEVERITY", "1. LOW", "2. MEDIUM", "3. HIGH", "4. CRITICAL");
        int sevChoice = InputHelper.getInt(scanner, "Choose severity (1-4, 0-Cancel): ", 0, 4);
        if (sevChoice == 0) {
            return null;
        }

        Incident.IncidentSeverity severity = Incident.IncidentSeverity.LOW;
        switch (sevChoice) {
            case 2: severity = Incident.IncidentSeverity.MEDIUM; break;
            case 3: severity = Incident.IncidentSeverity.HIGH; break;
            case 4: severity = Incident.IncidentSeverity.CRITICAL; break;
        }

        try {
            incidentService.reportIncident(id, title, description, severity);
            System.out.println("Incident reported with status OPEN.");
            return "[OK] Incident " + id + " reported with status OPEN.";
        } catch (exception.SiteManagementException e) {
            System.out.println("Error: " + e.getMessage());
            return "[ERROR] " + e.getMessage();
        }
    }

    private String assignIncidentFlow(Scanner scanner) {
        System.out.println("\n--- ASSIGN INCIDENT HANDLER ---");
        String incidentId = InputHelper.getString(scanner, "Enter Incident ID to assign (0 to cancel): ");
        if (incidentId.equals("0")) {
            return null;
        }
        try {
            Incident incident = incidentService.findIncidentById(incidentId);
            if (incident == null) {
                throw new exception.NotFoundException("Incident record", "ID", incidentId);
            }
            String assigneeCode = InputHelper.getString(scanner, "Enter Assignee Person Code (e.g., W01): ");
            incidentService.assignIncident(incidentId, assigneeCode);
            System.out.println("Incident '" + incidentId + "' assigned to '" + assigneeCode + "'. Status is now ASSIGNED.");
            return "[OK] Incident '" + incidentId + "' assigned to '" + assigneeCode + "'.";
        } catch (exception.NotFoundException | exception.AppError e) {
            System.out.println("Assignment Error: " + e.getMessage());
            return "[ERROR] Assignment Error: " + e.getMessage();
        }
    }

    private String resolveIncidentFlow(Scanner scanner) {
        System.out.println("\n--- RESOLVE INCIDENT ---");
        String incidentId = InputHelper.getString(scanner, "Enter Incident ID to resolve (0 to cancel): ");
        if (incidentId.equals("0")) {
            return null;
        }
        try {
            incidentService.resolveIncident(incidentId);
            System.out.println("Incident resolved.");
            return "[OK] Incident '" + incidentId + "' resolved.";
        } catch (exception.SiteManagementException e) {
            System.out.println("Incident State Error: " + e.getMessage());
            return "[ERROR] Incident State Error: " + e.getMessage();
        }
    }
}
