package controller;

import exception.AppError;
import exception.NotFoundException;
import model.Permission;
import model.Person;
import model.RestrictedZone;
import model.Zone;
import service.AuthenticationService;
import service.PersonnelService;
import service.ZoneService;
import service.ZoneRemovalService;
import view.MenuView;
import view.ConsoleUi;
import util.InputHelper;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class ZoneController {
    private final ZoneService zoneService;
    private final PersonnelService personnelService;
    private final AuthenticationService authentication;
    private final ZoneRemovalService zoneRemoval;

    public ZoneController(
            ZoneService zoneService, PersonnelService personnelService,
            AuthenticationService authentication, ZoneRemovalService zoneRemoval
    ) {
        this.zoneService = zoneService;
        this.personnelService = personnelService;
        this.authentication = authentication;
        this.zoneRemoval = zoneRemoval;
    }

    public void handleMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            MenuView.showZoneMenu(authentication.currentAccount());
            int choice = InputHelper.getInt(scanner, "Enter option (0-6): ", 0, 6);
            switch (choice) {
                case 1:
                    authentication.require(Permission.MANAGE_ZONES);
                    addZoneFlow(scanner);
                    break;
                case 2:
                    authentication.requireAny(Permission.MANAGE_ZONES, Permission.VIEW_OCCUPANCY);
                    displayZoneListFlow();
                    break;
                case 3:
                    authentication.require(Permission.MANAGE_ZONES);
                    changeAccessFlow(scanner, true);
                    break;
                case 4:
                    authentication.require(Permission.MANAGE_ZONES);
                    changeAccessFlow(scanner, false);
                    break;
                case 5:
                    authentication.require(Permission.MANAGE_ZONES);
                    updateZoneFlow(scanner);
                    break;
                case 6:
                    authentication.require(Permission.MANAGE_ZONES);
                    deleteZoneFlow(scanner);
                    break;
                case 0:
                    back = true;
                    break;
            }
        }
    }

    private void addZoneFlow(Scanner scanner) {
        System.out.println("\n--- ADD NEW ZONE ---");
        int type = InputHelper.getInt(scanner, "Select zone type: 1. Normal Zone | 2. Restricted Zone (0-Cancel): ", 0, 2);
        if (type == 0) return;

        String rawZoneId = InputHelper.getString(scanner, "Enter Zone ID (e.g., Z1, Z2): ");
        String zoneId = Zone.normalizeId(rawZoneId);
        String zoneName = InputHelper.getString(scanner, "Enter Zone Name: ");

        try {
            if (type == 1) {
                Zone zone = new Zone(zoneId, zoneName);
                if (zoneService.addZone(zone)) {
                    System.out.println("Successfully added zone: " + zone.getZoneName());
                }
            } else {
                ConsoleUi.menu("SELECT REQUIRED SAFETY LEVEL",
                        "1. LOW", "2. MEDIUM", "3. HIGH", "4. CRITICAL");
                int levelOpt = InputHelper.getInt(scanner, "Choice (1-4): ", 1, 4);

                model.SafetyLevel level;
                switch (levelOpt) {
                    case 1: level = model.SafetyLevel.LOW; break;
                    case 3: level = model.SafetyLevel.HIGH; break;
                    case 4: level = model.SafetyLevel.CRITICAL; break;
                    default: level = model.SafetyLevel.MEDIUM; break;
                }

                RestrictedZone restrictedZone = new RestrictedZone(zoneId, zoneName, level, new HashSet<>());
                if (zoneService.addZone(restrictedZone)) {
                    System.out.println("Successfully added zone: " + restrictedZone.getZoneName());
                }
            }
        } catch (AppError e) {
            System.out.println("Zone Error: " + e.getMessage());
        }
    }

    private void displayZoneListFlow() {
        System.out.println("\n--- ZONE LIST ---");
        List<Zone> zones = zoneService.getAllZones();
        if (zones.isEmpty()) {
            System.out.println("No zones available.");
            return;
        }
        String[] headers = { "ID", "NAME", "TYPE", "REQUIRED LEVEL", "ALLOWED COUNT" };
        List<String[]> rows = new java.util.ArrayList<>();
        for (Zone zone : zones) {
            String type = zone instanceof RestrictedZone ? "RESTRICTED" : "REGULAR";
            String level = zone instanceof RestrictedZone
                    ? ((RestrictedZone) zone).getRequiredSafetyLevel().name() : "-";
            String count = zone instanceof RestrictedZone
                    ? String.valueOf(((RestrictedZone) zone).getAllowedPersonCodes().size()) : "-";
            rows.add(new String[] { zone.getZoneId(), zone.getZoneName(), type, level, count });
        }
        ConsoleUi.table(headers, rows);
    }

    private void changeAccessFlow(Scanner scanner, boolean grant) {
        String zoneId = InputHelper.getString(scanner, "Enter Zone ID: ");
        try {
            zoneService.findZoneById(zoneId);
            String code = InputHelper.getString(scanner, "Enter Person Code: ");
            Person person = personnelService.findPersonByCode(code);
            boolean changed = grant
                    ? zoneService.grantZoneAccess(zoneId, person)
                    : zoneService.revokeZoneAccess(zoneId, person);
            System.out.println(changed
                    ? "Success: Access list of zone '" + zoneId + "' has been updated."
                    : "No change: The zone is not restricted or the person is not listed.");
        } catch (NotFoundException | AppError e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void updateZoneFlow(Scanner scanner) {
        String zoneId = InputHelper.getString(scanner, "Enter Zone ID: ");
        try {
            Zone zone = zoneService.findZoneById(zoneId);
            System.out.print("New Zone Name [" + zone.getZoneName() + "]: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                name = zone.getZoneName();
            }
            model.SafetyLevel level = null;
            if (zone instanceof RestrictedZone) {
                level = chooseSafetyLevel(scanner, ((RestrictedZone) zone).getRequiredSafetyLevel());
            }
            zoneService.updateZone(zoneId, name, level);
            System.out.println("[OK] Zone updated successfully.");
        } catch (exception.SiteManagementException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private model.SafetyLevel chooseSafetyLevel(Scanner scanner, model.SafetyLevel current) {
        ConsoleUi.menu("UPDATE SAFETY LEVEL",
                "Current: " + current, "1. LOW", "2. MEDIUM", "3. HIGH", "4. CRITICAL");
        int choice = InputHelper.getInt(scanner, "New level (1-4, 0 keep current): ", 0, 4);
        if (choice == 0) {
            return current;
        }
        switch (choice) {
            case 1: return model.SafetyLevel.LOW;
            case 2: return model.SafetyLevel.MEDIUM;
            case 3: return model.SafetyLevel.HIGH;
            default: return model.SafetyLevel.CRITICAL;
        }
    }

    private void deleteZoneFlow(Scanner scanner) {
        String zoneId = InputHelper.getString(scanner, "Enter Zone ID: ");
        try {
            zoneService.findZoneById(zoneId);
            int confirm = InputHelper.getInt(scanner, "Delete this zone? 1. Yes | 0. Cancel: ", 0, 1);
            if (confirm == 0) {
                return;
            }
            zoneRemoval.delete(zoneId);
            System.out.println("[OK] Zone deleted successfully.");
        } catch (exception.SiteManagementException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }
}
