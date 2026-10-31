package controller;

import model.incident.Incident;
import service.IncidentService;
import service.AuthenticationService;
import model.auth.Permission;
import view.MenuView;
import util.InputHelper;
import java.util.Scanner;

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
                    view.ListView.showTable(
                            scanner,
                            "INCIDENTS",
                            site.incidents(choice == 6),
                            view.IncidentView.TABLE_HEADERS,
                            view.IncidentView::tableRow,
                            view.IncidentView::formatSummary,
                            java.util.Comparator.comparing(
                                    Incident::getIncidentId, util.IdentifierHelper.NATURAL_ORDER
                            )
                    );
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
                    if (!InputHelper.confirm(scanner, "Are you sure you want to archive all resolved incidents?")) {
                        view.ConsoleTheme.warning("Archiving cancelled.");
                        break;
                    }
                    view.ConsoleTheme.success("Archived " + site.archiveIncidents() + " resolved incidents.");
                    break;
                case 0:
                    return;
            }
        }
    }

    private void reportIncidentFlow(Scanner scanner) {
        view.ConsoleTheme.breadcrumb("INCIDENTS / REPORT NEW INCIDENT");
        String defaultId = site.suggestNextIncidentId();
        String id = getValidatedIncidentId(scanner, defaultId);
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
        view.ConsoleTheme.breadcrumb("INCIDENTS / ASSIGN HANDLER");
        String incidentId = InputHelper.getString(scanner, "Enter Incident ID to assign: ");
        Incident target = site.incidents(false).stream()
                .filter(i -> i.getIncidentId().equalsIgnoreCase(incidentId))
                .findFirst().orElse(null);
        if (target == null) {
            view.ConsoleTheme.warning("Incident '" + incidentId + "' not found.");
            return;
        }
        if (target.getStatus() == Incident.IncidentStatus.RESOLVED) {
            view.ConsoleTheme.warning("Incident '" + incidentId + "' is already resolved.");
            return;
        }
        java.util.List<model.person.Person> activePeople = site.candidateAssignees().stream()
                .filter(model.person.Person::isActive)
                .collect(java.util.stream.Collectors.toList());
        if (!activePeople.isEmpty()) {
            System.out.println("Available assignees: " + activePeople.stream()
                    .map(p -> p.getCode() + " (" + p.getName() + " - " + p.getRole() + ")")
                    .collect(java.util.stream.Collectors.joining(", ")));
        }
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
        view.ConsoleTheme.breadcrumb("INCIDENTS / RESOLVE INCIDENT");
        String incidentId = InputHelper.getString(scanner, "Enter Incident ID to resolve: ");
        Incident target = site.incidents(false).stream()
                .filter(i -> i.getIncidentId().equalsIgnoreCase(incidentId))
                .findFirst().orElse(null);
        if (target == null) {
            view.ConsoleTheme.warning("Incident '" + incidentId + "' not found.");
            return;
        }
        if (target.getStatus() != Incident.IncidentStatus.ASSIGNED) {
            view.ConsoleTheme.warning("Only ASSIGNED incidents can be resolved. Current status: " + target.getStatus());
            return;
        }
        if (site.resolveIncident(incidentId)) {
            view.ConsoleTheme.success("Incident resolved.");
        } else {
            view.ConsoleTheme.warning("Invalid incident ID.");
        }
    }

    private String getValidatedIncidentId(Scanner scanner, String defaultId) {
        while (true) {
            String input = InputHelper.readLine(scanner, "Enter Incident ID (e.g., " + defaultId + ", blank for default): ").trim();
            if (input.isEmpty()) {
                input = defaultId;
            }
            try {
                String normalized = util.IdentifierHelper.normalize(input, "incidentId");
                boolean duplicate = site.incidents(true).stream().anyMatch(i -> i.getIncidentId().equalsIgnoreCase(normalized));
                if (duplicate) {
                    view.ConsoleTheme.warning("Incident ID '" + normalized + "' already exists. Please choose a different ID.");
                    continue;
                }
                return normalized;
            } catch (exception.DataValidationException e) {
                view.ConsoleTheme.warning("Invalid incident ID: " + e.getMessage());
            }
        }
    }
}
