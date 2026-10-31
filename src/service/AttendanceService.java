package service;

import model.attendance.AttendanceRecord;
import repository.AttendanceRepository;
import java.time.LocalDateTime;
import java.util.List;
import util.DateTimeHelper;

/**
 * Coordinates personnel lookup, zone access and attendance storage.
 *
 * <p>The repository supplies the previous swipe to the attendance model, which owns
 * the ordering rule. New record identifiers are derived from existing numeric
 * {@code REC} identifiers at construction and after successful CSV loading.
 */
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final PersonnelService personnelService;
    private final ZoneService zoneService;
    private java.math.BigInteger recordCounter = java.math.BigInteger.ONE;

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
        syncRecordCounter();
    }

    /**
     * Sets the next sequence above the highest existing numeric REC identifier.
     *
     * <p>Using the maximum avoids reusing an existing identifier after deletion.
     * Identifiers outside {@code REC} followed by positive digits do not contribute to this sequence.
     */
    private void syncRecordCounter() {
        java.math.BigInteger maxNumber = java.math.BigInteger.ZERO;
        for (AttendanceRecord record : attendanceRepository.findAll()) {
            String id = record.getRecordId();
            if (id != null && id.matches("REC[1-9]\\d*")) {
                maxNumber = maxNumber.max(new java.math.BigInteger(id.substring(3)));
            }
        }
        this.recordCounter = maxNumber.add(java.math.BigInteger.ONE);
    }

    /**
     * Checks references and access, validates swipe order, then stores a new record.
     *
     * <p>Zone access is verified on check-in. The sequence advances only when
     * the repository accepts the new record.
     */
    public boolean processAttendance(String personCode, String zoneId, AttendanceRecord.CheckType type)
            throws exception.AttendanceOrderException {
        AttendanceRecord record = createRecord(personCode, zoneId, type);
        if (record == null || !attendanceRepository.save(record)) {
            return false;
        }
        recordCounter = recordCounter.add(java.math.BigInteger.ONE);
        return true;
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
        if (type == AttendanceRecord.CheckType.CHECK_IN) {
            if (!person.isActive()) {
                throw new exception.DataValidationException("person", "Inactive personnel cannot check in.");
            }
            zoneService.verifyAccess(cleanZoneId, person);
        }
        String recordId = "REC" + recordCounter;
        String timestamp = DateTimeHelper.formatTimestamp(LocalDateTime.now());
        return new AttendanceRecord(recordId, cleanPersonCode, cleanZoneId, timestamp, type);
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

    /**
     * Commits one validated swipe to CSV before publishing it or consuming its identifier.
     * A failed validation or file replacement leaves the collection and sequence unchanged.
     */
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
        recordCounter = recordCounter.add(java.math.BigInteger.ONE);
        return true;
    }

    public List<AttendanceRecord> getAllAttendanceRecords() {
        return attendanceRepository.findAll();
    }

    public boolean loadFromCsv() {
        return loadFromCsv("data/attendance.csv");
    }

    public boolean loadFromCsv(String filePath) {
        boolean loaded = attendanceRepository.loadFromCsv(filePath);
        if (loaded) {
            syncRecordCounter();
        }
        return loaded;
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
