package controller;

import exception.DuplicateZoneIdException;
import exception.RestrictedZoneViolationException;
import exception.ZoneNotFoundException;
import model.RestrictedZone;
import model.Zone;
import service.ZoneService;
import service.AuthenticationService;
import model.Permission;
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
            int choice = InputHelper.getInt(scanner, "Enter option (0-4): ", 0, 4);
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
        String zoneId = InputHelper.getString(scanner, "Zone ID: ");
        String personCode = InputHelper.getString(scanner, "Person code: ");
        boolean changed = site.changeAccess(zoneId, personCode, grant);
        if (changed) {
            view.ConsoleTheme.success("Access list updated.");
        } else {
            view.ConsoleTheme.warning("Choose a restricted zone.");
        }
    }

    private void addZoneFlow(Scanner scanner) {
        view.ConsoleTheme.title("ADD NEW ZONE");
        int type = InputHelper.getInt(
                scanner,
                "Select zone type: 1. Normal Zone | 2. Restricted Zone (0-Cancel): ",
                0,
                2
        );
        if (type == 0) {
            return;
        }
        String zoneId = InputHelper.getString(scanner, "Enter Zone ID (e.g., Z01): ");
        String zoneName = InputHelper.getString(scanner, "Enter Zone Name: ");
        try {
            if (type == 1) {
                Zone zone = new Zone(zoneId, zoneName);
                if (site.addZone(zone)) {
                    view.ConsoleTheme.success("Zone added.");
                }
            } else {
                System.out.println("Select Safety Level: 1. LOW | 2. MEDIUM | 3. HIGH | 4. CRITICAL");
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

    private void displayZoneListFlow(Scanner scanner) {
        view.ListView.show(scanner, "ZONES", site.zones(), Zone::toString, Zone::toString,
                java.util.Comparator.comparing(Zone::getZoneId, util.IdentifierHelper.NATURAL_ORDER),
                            event -> site.getAuthentication().audit(event, "LIST", "", "SUCCESS", "Navigated list."));
    }
}
