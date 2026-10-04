package controller;

import model.Incident;
import service.IncidentService;
import view.MenuView;
import util.InputHelper;
import java.util.Scanner;

/**
 * TẦNG ĐIỀU KHIỂN (CONTROLLER LAYER)
 * PHỤ TRÁCH: Thành viên 4 (Nguyễn Tấn Lợi - SE211059)
 * MÔ TẢ:
 * - Điều hướng luồng tương tác Console cho Menu Quản lý Sự cố An toàn.
 * - Thu thập input, gọi IncidentService và hiển thị kết quả ra giao diện.
 */
public class IncidentController {
    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    public void handleMenu(Scanner scanner) {
        boolean back = false;
        while (!back) {
            MenuView.showIncidentMenu();
            int choice = InputHelper.getInt(scanner, "Enter option (0-5): ", 0, 5);
            switch (choice) {
                case 1:
                    reportIncidentFlow(scanner);
                    break;
                case 2:
                    incidentService.displayAllIncidents();
                    break;
                case 3:
                    assignIncidentFlow(scanner);
                    break;
                case 4:
                    resolveIncidentFlow(scanner);
                    break;
                case 5:
                    incidentService.purgeResolvedAndNotify();
                    break;
                case 0:
                    back = true;
                    break;
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
            case 2: severity = Incident.IncidentSeverity.MEDIUM; break;
            case 3: severity = Incident.IncidentSeverity.HIGH; break;
            case 4: severity = Incident.IncidentSeverity.CRITICAL; break;
        }

        incidentService.reportIncident(id, title, description, severity);
    }

    private void assignIncidentFlow(Scanner scanner) {
        System.out.println("\n--- ASSIGN INCIDENT HANDLER ---");
        String incidentId = InputHelper.getString(scanner, "Enter Incident ID to assign: ");
        String assigneeCode = InputHelper.getString(scanner, "Enter Assignee Person Code (e.g., EMP001): ");
        incidentService.assignIncident(incidentId, assigneeCode);
    }

    private void resolveIncidentFlow(Scanner scanner) {
        System.out.println("\n--- RESOLVE INCIDENT ---");
        String incidentId = InputHelper.getString(scanner, "Enter Incident ID to resolve: ");
        incidentService.resolveIncidentAndNotify(incidentId);
    }
}
