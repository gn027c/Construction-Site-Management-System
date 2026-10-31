package service;

import model.attendance.AttendanceRecord;
import repository.AttendanceRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import util.DateTimeHelper;

public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final PersonnelService personnelService;
    private final ZoneService zoneService;

    public AttendanceService(PersonnelService personnelService, ZoneService zoneService) {
        this(new AttendanceRepository(), personnelService, zoneService);
    }

    public AttendanceService(
            AttendanceRepository attendanceRepository,
            PersonnelService personnelService,
            ZoneService zoneService
    ) {
        this.attendanceRepository = (attendanceRepository != null) ? attendanceRepository : new AttendanceRepository();
        this.personnelService = personnelService;
        this.zoneService = zoneService;
    }

    public boolean processAttendance(String personCode, String zoneId, AttendanceRecord.CheckType type)
            throws exception.AttendanceOrderException {
        AttendanceRecord record = createRecord(personCode, zoneId, type);
        return record != null && attendanceRepository.save(record);
    }

    private AttendanceRecord createRecord(String personCode, String zoneId, AttendanceRecord.CheckType type) {
        if (personCode == null
                || personCode.trim().isEmpty()
                || zoneId == null
                || zoneId.trim().isEmpty()
                || type == null) {
            return null;
        }
        String cleanPersonCode = util.IdentifierHelper.normalize(personCode, "personCode");
        String cleanZoneId = util.IdentifierHelper.normalize(zoneId, "zoneId");
        model.person.Person person = personnelService.findPersonByCode(cleanPersonCode);
        zoneService.findZoneById(cleanZoneId);
        LocalDateTime now = LocalDateTime.now();
        if (type == AttendanceRecord.CheckType.CHECK_IN) {
            if (!person.isActive()) {
                throw new exception.DataValidationException("person", "Inactive personnel cannot check in.");
            }
            String dateProblem = person.checkInDenialReason(now.toLocalDate());
            if (!dateProblem.isEmpty()) {
                throw new exception.DataValidationException("visitDate", dateProblem);
            }
            zoneService.verifyAccess(cleanZoneId, person);
        }
        String recordId = util.IdentifierHelper.next("REC", recordIds(), 0);
        String timestamp = DateTimeHelper.formatTimestamp(now);
        return new AttendanceRecord(recordId, cleanPersonCode, cleanZoneId, timestamp, type);
    }

    private List<String> recordIds() {
        List<String> ids = new ArrayList<>();
        for (AttendanceRecord record : attendanceRepository.findAll()) {
            ids.add(record.getRecordId());
        }
        return ids;
    }

    public PersonnelService getPersonnelService() {
        return personnelService;
    }

    public ZoneService getZoneService() {
        return zoneService;
    }

    public boolean recordAttendance(String personCode, String zoneId, AttendanceRecord.CheckType type)
            throws exception.AttendanceOrderException {
        return processAttendance(personCode, zoneId, type);
    }

    public boolean recordAttendanceAndSave(
            String personCode, String zoneId, AttendanceRecord.CheckType type, String file
    ) {
        AttendanceRecord record = createRecord(personCode, zoneId, type);
        if (record == null) {
            return false;
        }
        if (!attendanceRepository.saveAndPersist(record, file)) {
            throw new exception.SiteManagementException(
                    "Attendance was not saved; no swipe was recorded. Retry after fixing storage."
            );
        }
        return true;
    }

    public List<AttendanceRecord> getAllAttendanceRecords() {
        return attendanceRepository.findAll();
    }

    public boolean loadFromCsv() {
        return loadFromCsv("data/attendance.csv");
    }

    public boolean loadFromCsv(String filePath) {
        return attendanceRepository.loadFromCsv(filePath);
    }

    public boolean saveToCsv() {
        return saveToCsv("data/attendance.csv");
    }

    public boolean saveToCsv(String filePath) {
        return attendanceRepository.saveToCsv(filePath);
    }

    public String getLastLoadError() {
        return attendanceRepository.getLastLoadError();
    }
}
