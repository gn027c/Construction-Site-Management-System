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
    private final SiteManagerController site;
    private final AuthenticationService authentication;

    public IncidentController(SiteManagerController site) {
        this.site = site;
        authentication = site.getAuthentication();
    }

    public void handleMenu(Scanner scanner) {
        while (true) {
            MenuView.showIncidentMenu(authentication);
            int choice = InputHelper.getMenuChoice(scanner, "Enter option (0-6): ", 6);
            view.ConsoleScreen.begin();
            switch (choice) {
                case 1:
                    authentication.require(Permission.REPORT_INCIDENTS);
                    reportIncidentFlow(scanner);
                    break;
                case 2:
                case 6:
                    view.ListView.show(scanner, "INCIDENTS", site.incidents(choice == 6),
                            Incident::toString, Incident::toString,
                            java.util.Comparator.comparing(
                                    Incident::getIncidentId, util.IdentifierHelper.NATURAL_ORDER
                            ),
                            event -> site.getAuthentication().audit(event, "LIST", "", "SUCCESS", "Navigated list."));
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
                    view.ConsoleTheme.success("Archived " + site.archiveIncidents() + " resolved incidents.");
                    break;
                case 0:
                    return;
            }
        }
    }

    private void reportIncidentFlow(Scanner scanner) {
        view.ConsoleTheme.title("REPORT NEW INCIDENT");
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
        if (site.reportIncident(id, title, description, severity)) {
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
            if (site.assignIncident(incidentId, assigneeCode)) {
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
        if (site.resolveIncident(incidentId)) {
            view.ConsoleTheme.success("Incident resolved.");
        } else {
            view.ConsoleTheme.warning("Invalid incident ID.");
        }
    }
}
