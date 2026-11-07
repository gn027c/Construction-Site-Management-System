package service;

import exception.AppError;
import model.Account;
import model.Permission;
import model.Person;

public class PersonRemovalService {
    private final PersonnelService personnelService;
    private final ZoneService zoneService;
    private final AttendanceService attendanceService;
    private final IncidentService incidentService;
    private final AuthenticationService authentication;

    public PersonRemovalService(
            PersonnelService personnelService, ZoneService zoneService, AttendanceService attendanceService,
            IncidentService incidentService, AuthenticationService authentication
    ) {
        this.personnelService = personnelService;
        this.zoneService = zoneService;
        this.attendanceService = attendanceService;
        this.incidentService = incidentService;
        this.authentication = authentication;
    }

    public void delete(String code) {
        authentication.require(Permission.MANAGE_PERSONNEL);
        Person person = personnelService.findPersonByCode(code);
        if (attendanceService.hasRecords(person.getCode())) {
            throw new AppError("Person " + person.getCode() + " has attendance records.");
        }
        if (zoneService.isListed(person.getCode())) {
            throw new AppError("Person " + person.getCode() + " is on a restricted zone access list.");
        }
        if (incidentService.isAssignedTo(person.getCode())) {
            throw new AppError("Person " + person.getCode() + " is assigned to an incident.");
        }
        Account account = authentication.accountOf(person.getCode());
        if (account != null) {
            authentication.removeLinkedAccount(account);
        }
        personnelService.deletePerson(person.getCode());
        authentication.persistPersonData();
    }
}
