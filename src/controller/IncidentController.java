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
        String zoneId = selectIncidentZone(scanner);
        if (site.reportIncident(id, title, description, severity, zoneId)) {
            view.ConsoleTheme.success("Incident reported with status OPEN.");
        } else {
            view.ConsoleTheme.warning("Invalid incident data or duplicate ID.");
        }
    }

    private void assignIncidentFlow(Scanner scanner) {
        view.ConsoleTheme.breadcrumb("INCIDENTS / ASSIGN HANDLER");
        String incidentId = InputHelper.getString(scanner, "Enter Incident ID to assign: ");
        Incident target = findActiveIncident(incidentId);
        if (target == null) {
            view.ConsoleTheme.warning("Incident '" + incidentId + "' not found.");
            return;
        }
        if (target.getStatus() == Incident.IncidentStatus.RESOLVED) {
            view.ConsoleTheme.warning("Incident '" + incidentId + "' is already resolved.");
            return;
        }
        if (target.getStatus() == Incident.IncidentStatus.ASSIGNED) {
            System.out.println(
                    "Currently assigned to " + target.getAssignedTo() + "; choose another person to reassign."
            );
        }
        java.util.List<model.person.Person> candidates = site.candidateAssignees();
        if (candidates.isEmpty()) {
            view.ConsoleTheme.warning("No active site personnel can handle incidents.");
            return;
        }
        System.out.println("Available assignees: " + candidates.stream()
                .map(p -> p.getCode() + " (" + p.getName() + " - " + p.getRole() + ")")
                .collect(java.util.stream.Collectors.joining(", ")));
        String assigneeCode = InputHelper.getString(
                scanner, "Enter Assignee Person Code (e.g., " + candidates.get(0).getCode() + "): "
        );
        String assignee = util.IdentifierHelper.normalizeForLookup(assigneeCode);
        if (candidates.stream().noneMatch(p -> p.getCode().equals(assignee))) {
            view.ConsoleTheme.warning("Choose an active person from the list of available assignees.");
            return;
        }
        try {
            site.assignIncident(target.getIncidentId(), assignee);
            view.ConsoleTheme.success("Incident " + target.getIncidentId() + " assigned to " + assignee + ".");
        } catch (exception.SiteManagementException e) {
            view.ConsoleTheme.warning("Assignment Error: " + e.getMessage());
        }
    }

    private String selectIncidentZone(Scanner scanner) {
        java.util.List<model.zone.Zone> zones = site.incidentZones();
        System.out.println("Zones: " + zones.stream()
                .map(z -> z.getZoneId() + " (" + z.getZoneName() + ")")
                .collect(java.util.stream.Collectors.joining(", ")));
        while (true) {
            String input = InputHelper.getString(scanner, "Zone ID where it happened: ");
            String key = util.IdentifierHelper.normalizeForLookup(input);
            for (model.zone.Zone zone : zones) {
                if (zone.getZoneId().equals(key)) {
                    return zone.getZoneId();
                }
            }
            view.ConsoleTheme.warning("Choose a zone from the list.");
        }
    }

    private Incident findActiveIncident(String incidentId) {
        String key = util.IdentifierHelper.normalizeForLookup(incidentId);
        return site.incidents(false).stream()
                .filter(i -> i.getIncidentId().equals(key))
                .findFirst().orElse(null);
    }

    private void resolveIncidentFlow(Scanner scanner) {
        view.ConsoleTheme.breadcrumb("INCIDENTS / RESOLVE INCIDENT");
        String incidentId = InputHelper.getString(scanner, "Enter Incident ID to resolve: ");
        Incident target = findActiveIncident(incidentId);
        if (target == null) {
            view.ConsoleTheme.warning("Incident '" + incidentId + "' not found.");
            return;
        }
        if (target.getStatus() != Incident.IncidentStatus.ASSIGNED) {
            view.ConsoleTheme.warning("Only ASSIGNED incidents can be resolved. Current status: " + target.getStatus());
            return;
        }
        String note = InputHelper.getString(scanner, "Resolution note (what was done): ");
        if (site.resolveIncident(target.getIncidentId(), note)) {
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
