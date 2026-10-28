package state;

import service.*;
import model.*;
import java.util.*;

/** One in-memory unit of work. Console operations use a single owner thread. */
public final class AppState {
    public final PersonnelService personnel = new PersonnelService();
    public final ZoneService zones = new ZoneService();
    public final AttendanceService attendance = new AttendanceService(personnel, zones);
    public final IncidentService incidents = new IncidentService(personnel);

    /** Check references before publishing a complete new state. */
    public void validate() {
        for (AttendanceRecord record : attendance.getAllAttendanceRecords()) {
            personnel.findPersonByCode(record.getPersonCode());
            zones.findZoneById(record.getZoneId());
        }
        for (Zone zone : zones.getAllZones()) {
            if (zone instanceof RestrictedZone) {
                for (String code : ((RestrictedZone)zone).getAllowedPersonCodes()) personnel.findPersonByCode(code);
            }
        }
        for (Incident incident : incidents.getAllIncidents()) {
            boolean open = incident.getStatus() == Incident.IncidentStatus.OPEN;
            if (open != incident.getAssignedTo().isEmpty()) throw new IllegalStateException("Invalid incident assignment: " + incident.getIncidentId());
            if (!open) personnel.findPersonByCode(incident.getAssignedTo());
        }
    }
}
