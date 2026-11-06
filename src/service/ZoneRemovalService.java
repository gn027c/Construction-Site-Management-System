package service;

import exception.AppError;
import exception.NotFoundException;
import model.Permission;
import model.Zone;

public class ZoneRemovalService {
    private final ZoneService zoneService;
    private final AttendanceService attendanceService;
    private final AuthenticationService authentication;

    public ZoneRemovalService(ZoneService zoneService, AttendanceService attendanceService) {
        this(zoneService, attendanceService, null);
    }

    public ZoneRemovalService(
            ZoneService zoneService, AttendanceService attendanceService, AuthenticationService authentication
    ) {
        this.zoneService = util.ValidationHelper.requireNotNull(zoneService, "zoneService", "Must not be null.");
        this.attendanceService = util.ValidationHelper.requireNotNull(attendanceService, "attendanceService", "Must not be null.");
        this.authentication = authentication;
    }

    public void delete(String zoneId) throws NotFoundException, AppError {
        if (authentication != null) {
            authentication.require(Permission.MANAGE_ZONES);
        }
        Zone zone = zoneService.findZoneById(zoneId);
        if (attendanceService.hasRecordsForZone(zone.getZoneId())) {
            throw new AppError("Zone " + zone.getZoneId() + " has attendance history and cannot be deleted.");
        }
        zoneService.deleteZone(zone.getZoneId());
    }
}

