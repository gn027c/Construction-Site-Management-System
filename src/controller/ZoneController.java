package controller;

import exception.DuplicateZoneIdException;
import exception.RestrictedZoneViolationException;
import exception.ZoneNotFoundException;
import model.zone.RestrictedZone;
import model.zone.Zone;
import service.ZoneService;
import service.AuthenticationService;
import model.auth.Permission;
import view.MenuView;
import util.InputHelper;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

/**
 * Handles console input and result messages for zones and access permissions.
 *
 * <p>Zone rules are delegated to {@link service.ZoneService}. Permission flows
 * verify personnel registration before changing a restricted zone's whitelist.
 */
public class ZoneController {
    private final SiteManagerController site;

    public ZoneController(SiteManagerController site) {
        this.site = site;
    }

    public void handleMenu(Scanner scanner) {
        site.getAuthentication().require(Permission.MANAGE_ZONES);
        while (true) {
            MenuView.showZoneMenu();
            int choice = InputHelper.getMenuChoice(scanner, "Enter option (0-4): ", 4);
            view.ConsoleScreen.begin();
            switch (choice) {
                case 1:
                    addZoneFlow(scanner);
                    break;
                case 2:
                    displayZoneListFlow(scanner);
                    break;
                case 3:
                case 4:
                    changeAccessFlow(scanner, choice == 3);
                    break;
                case 0:
                    return;
            }
        }
    }

    private void changeAccessFlow(Scanner scanner, boolean grant) {
        view.ConsoleTheme.breadcrumb("ZONES / " + (grant ? "GRANT ACCESS" : "REVOKE ACCESS"));
        List<Zone> restrictedZones = site.zones().stream()
                .filter(z -> z instanceof RestrictedZone)
                .collect(java.util.stream.Collectors.toList());
        if (!restrictedZones.isEmpty()) {
            System.out.println("Restricted zones: " + restrictedZones.stream()
                    .map(z -> z.getZoneId() + " (" + z.getZoneName() + ")")
                    .collect(java.util.stream.Collectors.joining(", ")));
        }
        String zoneId = InputHelper.getString(scanner, "Zone ID: ");
        Zone targetZone = site.zones().stream()
                .filter(z -> z.getZoneId().equalsIgnoreCase(zoneId))
                .findFirst().orElse(null);
        if (targetZone == null) {
            view.ConsoleTheme.error("Zone '" + zoneId + "' not found.");
            return;
        }
        if (!(targetZone instanceof RestrictedZone)) {
            view.ConsoleTheme.warning("Choose a restricted zone.");
            return;
        }
        RestrictedZone rz = (RestrictedZone) targetZone;
        if (!grant && !rz.getAllowedPersonCodes().isEmpty()) {
            System.out.println("Currently authorized personnel: " + String.join(", ", rz.getAllowedPersonCodes()));
        }
        String personCode = InputHelper.getString(scanner, "Person code: ");
        try {
            site.person(personCode);
        } catch (exception.PersonNotFoundException e) {
            view.ConsoleTheme.error(e.getMessage());
            return;
        }
        if (!grant && !InputHelper.confirm(scanner, "Are you sure you want to revoke access for '" + personCode + "' from zone '" + zoneId + "'?")) {
            view.ConsoleTheme.warning("Revocation cancelled.");
            return;
        }
        boolean changed = site.changeAccess(zoneId, personCode, grant);
        if (changed) {
            view.ConsoleTheme.success("Access list updated.");
        } else {
            view.ConsoleTheme.warning("Choose a restricted zone.");
        }
    }

    private void addZoneFlow(Scanner scanner) {
        view.ConsoleTheme.breadcrumb("ZONES / ADD NEW ZONE");
        int type = InputHelper.getMenuChoice(
                scanner,
                "Select zone type: 1. Normal Zone │ 2. Restricted Zone (0-Cancel): ",
                2
        );
        if (type == 0) {
            return;
        }
        String defaultZoneId = site.suggestNextZoneId();
        String zoneId = getValidatedZoneId(scanner, defaultZoneId);
        String zoneName = InputHelper.getString(scanner, "Enter Zone Name: ");
        try {
            if (type == 1) {
                Zone zone = new Zone(zoneId, zoneName);
                if (site.addZone(zone)) {
                    view.ConsoleTheme.success("Zone added.");
                }
            } else {
                System.out.println("Select Safety Level: 1. LOW │ 2. MEDIUM │ 3. HIGH │ 4. CRITICAL");
                int levelOpt = InputHelper.getInt(scanner, "Choice (1-4): ", 1, 4);
                RestrictedZone.SafetyLevel level;
                switch (levelOpt) {
                    case 1:
                        level = RestrictedZone.SafetyLevel.LOW;
                        break;
                    case 3:
                        level = RestrictedZone.SafetyLevel.HIGH;
                        break;
                    case 4:
                        level = RestrictedZone.SafetyLevel.CRITICAL;
                        break;
                    default:
                        level = RestrictedZone.SafetyLevel.MEDIUM;
                        break;
                }
                RestrictedZone restrictedZone = new RestrictedZone(zoneId, zoneName, level, new HashSet<>());
                if (site.addZone(restrictedZone)) {
                    view.ConsoleTheme.success("Restricted zone added.");
                }
            }
        } catch (DuplicateZoneIdException e) {
            view.ConsoleTheme.error("Zone Error: " + e.getMessage());
        }
    }

    private String getValidatedZoneId(Scanner scanner, String defaultZoneId) {
        while (true) {
            String input = InputHelper.readLine(scanner, "Enter Zone ID (e.g., " + defaultZoneId + ", blank for default): ").trim();
            if (input.isEmpty()) {
                return defaultZoneId;
            }
            try {
                String normalized = util.IdentifierHelper.normalize(input, "zoneId");
                boolean duplicate = site.zones().stream().anyMatch(z -> z.getZoneId().equalsIgnoreCase(normalized));
                if (duplicate) {
                    view.ConsoleTheme.warning("Zone ID '" + normalized + "' already exists. Please choose a different ID.");
                    continue;
                }
                return normalized;
            } catch (exception.DataValidationException e) {
                view.ConsoleTheme.warning("Invalid zone ID: " + e.getMessage());
            }
        }
    }

    private void displayZoneListFlow(Scanner scanner) {
        view.ListView.showTable(
                scanner,
                "ZONES",
                site.zones(),
                view.ZoneView.TABLE_HEADERS,
                view.ZoneView::tableRow,
                view.ZoneView::formatSummary,
                java.util.Comparator.comparing(Zone::getZoneId, util.IdentifierHelper.NATURAL_ORDER)
        );
    }
}
