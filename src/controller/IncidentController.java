package controller;

import model.Incident;
import service.IncidentService;
import service.AuthenticationService;
import model.Permission;
import view.MenuView;
import util.InputHelper;
import java.util.Scanner;

/**
 * Handles console input and result messages for incident operations.
 *
 * <p>Reporting, assignment, resolution and removal of resolved incidents are
 * delegated to {@link service.IncidentService}.
 */
public class IncidentController {
    private final IncidentService incidentService;
    private final AuthenticationService authentication;

    public IncidentController(IncidentService incidentService, AuthenticationService authentication) {
        this.incidentService = incidentService;
        this.authentication = authentication;
    }

    public void handleMenu(Scanner scanner) {
        handleMenu(scanner, () -> {
        });
    }

    /**
     * Runs the submenu and calls the supplied action after modifying menu choices.
     *
     * <p>The callback runs after the flow returns, including cancellation or handled
     * errors. Exceptions escaping the flow prevent the callback from running.
     */
    public void handleMenu(Scanner scanner, Runnable saveChanges) {
        authentication.requireAny(
                Permission.REPORT_INCIDENTS, Permission.VIEW_INCIDENTS, Permission.ASSIGN_INCIDENTS,
                Permission.RESOLVE_INCIDENTS, Permission.PURGE_INCIDENTS
        );
        boolean back = false;
        while (!back) {
            MenuView.showIncidentMenu(authentication);
            int choice = InputHelper.getInt(scanner, "Enter option (0-5): ", 0, 5);
            switch (choice) {
                case 1:
                    authentication.require(Permission.REPORT_INCIDENTS);
                    reportIncidentFlow(scanner);
                    break;
                case 2:
                    authentication.require(Permission.VIEW_INCIDENTS);
                    java.util.List<Incident> incidents = incidentService.getAllIncidents();
                    if (incidents.isEmpty()) {
                        view.ConsoleTheme.warning("No incidents.");
                    }
                    for (Incident incident : incidents) {
                        System.out.println(incident);
                    }
                    break;
                case 3:
                    authentication.require(Permission.ASSIGN_INCIDENTS);
                    assignIncidentFlow(scanner);
                    break;
                case 4:
                    authentication.require(Permission.RESOLVE_INCIDENTS);
                    resolveIncidentFlow(scanner);
                    break;
                case 5:
                    authentication.require(Permission.PURGE_INCIDENTS);
                    view.ConsoleTheme.success(
                            "Purged " + incidentService.purgeResolvedIncidents() + " resolved incidents."
                    );
                    break;
                case 0:
                    back = true;
                    break;
            }
            if (choice == 1 || choice == 3 || choice == 4 || choice == 5) {
                saveChanges.run();
            }
        }
    }

    private void reportIncidentFlow(Scanner scanner) {
        System.out.println("\n--- REPORT NEW INCIDENT ---");
        String id = InputHelper.getString(scanner, "Enter Incident ID (e.g., INC001): ");
        String title = InputHelper.getString(scanner, "Enter Incident Title: ");
        String description = InputHelper.getString(scanner, "Enter Incident Description: ");
        System.out.println("Select Severity Level:");
        System.out.println("1. LOW");
        System.out.println("2. MEDIUM");
        System.out.println("3. HIGH");
        System.out.println("4. CRITICAL");
        int sevChoice = InputHelper.getInt(scanner, "Choose severity (1-4): ", 1, 4);
        Incident.IncidentSeverity severity = Incident.IncidentSeverity.LOW;
        switch (sevChoice) {
            case 2:
                severity = Incident.IncidentSeverity.MEDIUM;
                break;
            case 3:
                severity = Incident.IncidentSeverity.HIGH;
                break;
            case 4:
                severity = Incident.IncidentSeverity.CRITICAL;
                break;
        }
        if (incidentService.reportIncident(id, title, description, severity)) {
            view.ConsoleTheme.success("Incident reported with status OPEN.");
        } else {
            view.ConsoleTheme.warning("Invalid incident data or duplicate ID.");
        }
    }

    private void assignIncidentFlow(Scanner scanner) {
        System.out.println("\n--- ASSIGN INCIDENT HANDLER ---");
        String incidentId = InputHelper.getString(scanner, "Enter Incident ID to assign: ");
        String assigneeCode = InputHelper.getString(scanner, "Enter Assignee Person Code (e.g., EMP001): ");
        try {
            if (incidentService.assignIncident(incidentId, assigneeCode)) {
                view.ConsoleTheme.success("Incident assigned.");
            } else {
                view.ConsoleTheme.warning("Assignee must be an existing person.");
            }
        } catch (exception.IncidentNotFoundException | exception.InvalidIncidentStateException e) {
            view.ConsoleTheme.warning("Assignment Error: " + e.getMessage());
        }
    }

    private void resolveIncidentFlow(Scanner scanner) {
        System.out.println("\n--- RESOLVE INCIDENT ---");
        String incidentId = InputHelper.getString(scanner, "Enter Incident ID to resolve: ");
        if (incidentService.resolveIncident(incidentId)) {
            view.ConsoleTheme.success("Incident resolved.");
        } else {
            view.ConsoleTheme.warning("Invalid incident ID.");
        }
    }
}
