package controller;

import exception.AppError;
import repository.AccountRepository;
import service.AuthenticationService;
import service.PersonRemovalService;
import service.ZoneRemovalService;
import service.PersonnelService;
import service.ZoneService;
import service.AttendanceService;
import service.IncidentService;
import service.ReportService;
import java.util.Scanner;

public class SiteManagerController {

    private final PersonnelService personnelService;
    private final ZoneService zoneService;
    private final AttendanceService attendanceService;
    private final IncidentService incidentService;

    private final PersonnelController personnelController;
    private final ZoneController zoneController;
    private final AttendanceController attendanceController;
    private final IncidentController incidentController;
    private final ReportController reportController;
    private final AccountController accountController;
    private final AuthenticationService authentication;
    private final util.DataPaths dataPaths;
    private boolean dataReady;
    private boolean accountsReady;
    private String loadProblem;

    public SiteManagerController() {
        this(System.getProperty("smartsite.dataDir", "data"));
    }

    public SiteManagerController(String dataDirectory) {
        this.dataPaths = new util.DataPaths(dataDirectory);

        this.personnelService = new PersonnelService();
        this.zoneService = new ZoneService();
        this.attendanceService = new AttendanceService(personnelService, zoneService);
        this.incidentService = new IncidentService(personnelService);
        this.authentication = new AuthenticationService(new AccountRepository(), personnelService, dataPaths.accounts());

        this.personnelController = new PersonnelController(
                personnelService, authentication,
                new PersonRemovalService(personnelService, zoneService, attendanceService, incidentService, authentication));
        this.zoneController = new ZoneController(
                zoneService, personnelService, authentication,
                new ZoneRemovalService(zoneService, attendanceService, authentication));
        this.attendanceController = new AttendanceController(attendanceService, personnelService, authentication);
        this.incidentController = new IncidentController(incidentService, authentication);
        this.reportController = new ReportController(
                new ReportService(attendanceService, incidentService), authentication);
        this.accountController = new AccountController(authentication);
    }

    public void loadAllData() {
        dataReady = false;
        accountsReady = false;
        loadProblem = null;
        try {
            authentication.load();
            accountsReady = true;
            personnelService.loadFromCsv(dataPaths.persons());
            zoneService.loadFromCsv(dataPaths.zones());
            attendanceService.loadFromCsv(dataPaths.attendance());
            incidentService.loadFromCsv(dataPaths.incidents());
            dataReady = true;
        } catch (RuntimeException e) {
            loadProblem = e.getMessage();
            throw e;
        }
    }

    public boolean isDataReady() {
        return dataReady;
    }

    public boolean isAccountsReady() {
        return accountsReady;
    }

    public String loadProblem() {
        return loadProblem;
    }

    public AuthenticationService authentication() {
        return authentication;
    }

    public PersonnelService personnel() {
        return personnelService;
    }

    public void handleAccountMenu(Scanner scanner) {
        accountController.handleMenu(scanner);
    }

    public util.DataPaths getDataPaths() {
        return dataPaths;
    }

    public String path(String fileName) {
        return dataPaths.path(fileName);
    }

    public void saveAllData() {
        requireDataReady();
        boolean saved = personnelService.saveToCsv(dataPaths.persons());
        saved &= zoneService.saveToCsv(dataPaths.zones());
        saved &= attendanceService.saveToCsv(dataPaths.attendance());
        saved &= incidentService.saveToCsv(dataPaths.incidents());
        if (!saved) {
            throw new AppError("Could not write every data file. Check the data directory and save again.");
        }
    }

    private void requireDataReady() {
        if (!dataReady) {
            throw new AppError("Data is not loaded. Fix the reported CSV file, then reload from menu 5.");
        }
    }

    public void handlePersonMenu(Scanner scanner) {
        requireDataReady();
        personnelController.handleMenu(scanner);
    }

    public void handleZoneMenu(Scanner scanner) {
        requireDataReady();
        zoneController.handleMenu(scanner);
    }

    public void handleAttendanceMenu(Scanner scanner) {
        requireDataReady();
        attendanceController.handleMenu(scanner);
    }

    public void handleIncidentMenu(Scanner scanner) {
        requireDataReady();
        incidentController.handleMenu(scanner);
    }

    public void handleReportMenu(Scanner scanner) {
        requireDataReady();
        reportController.handleMenu(scanner);
    }
}
