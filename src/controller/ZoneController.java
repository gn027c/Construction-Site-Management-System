package controller;

import exception.DuplicateZoneIdException;
import exception.RestrictedZoneViolationException;
import exception.ZoneNotFoundException;
import model.RestrictedZone;
import model.Zone;
import service.ZoneService;
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
 *
 * <p>Responsibility: Le Tan Thien (SE201852) - zone console flows.
 */
public class ZoneController {
    private final ZoneService zoneService;
    private final service.PersonnelService personnelService;

    public ZoneController(ZoneService zoneService, service.PersonnelService personnelService) {
        this.personnelService = personnelService;
        this.zoneService = zoneService;
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
        boolean back = false;
        while (!back) {
            MenuView.showZoneMenu();
            int choice = InputHelper.getInt(scanner, "Enter option (0-4): ", 0, 4);
            switch (choice) {
                case 1:
                    addZoneFlow(scanner);
                    break;
                case 2:
                    displayZoneListFlow();
                    break;
                case 3:
                case 4:
                    changeAccessFlow(scanner, choice == 3);
                    break;
                case 0:
                    back = true;
                    break;
            }
            if (choice == 1 || choice == 3 || choice == 4) {
                saveChanges.run();
            }
        }
    }

    private void changeAccessFlow(Scanner scanner, boolean grant) {
        String zoneId = InputHelper.getString(scanner, "Zone ID: ");
        String personCode = InputHelper.getString(scanner, "Person code: ");
        personnelService.findPersonByCode(personCode);
        boolean changed = grant
                ? zoneService.grantZoneAccess(zoneId, personCode)
                : zoneService.revokeZoneAccess(zoneId, personCode);
        System.out.println(changed ? "Access list updated." : "Choose a restricted zone.");
    }

    private void addZoneFlow(Scanner scanner) {
        System.out.println("\n--- ADD NEW ZONE ---");
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
                if (zoneService.addZone(zone)) {
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
                if (zoneService.addZone(restrictedZone)) {
                    view.ConsoleTheme.success("Restricted zone added.");
                }
            }
        } catch (DuplicateZoneIdException e) {
            view.ConsoleTheme.error("Zone Error: " + e.getMessage());
        }
    }

    private void displayZoneListFlow() {
        System.out.println("\n--- ZONE LIST ---");
        List<Zone> zones = zoneService.getAllZones();
        if (zones.isEmpty()) {
            System.out.println("No zones available.");
        } else {
            for (Zone z : zones) {
                System.out.println(z.toString());
            }
        }
    }
}
