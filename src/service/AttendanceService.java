package service;

import model.AttendanceRecord;
import repository.AttendanceRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final PersonnelService personnelService;
    private final ZoneService zoneService;
    private int recordCounter = 1;

    public AttendanceService(PersonnelService personnelService, ZoneService zoneService) {
        this(new AttendanceRepository(), personnelService, zoneService);
    }

    public AttendanceService(AttendanceRepository attendanceRepository,
                             PersonnelService personnelService,
                             ZoneService zoneService) {
        this.attendanceRepository = (attendanceRepository != null) ? attendanceRepository : new AttendanceRepository();
        this.personnelService = personnelService;
        this.zoneService = zoneService;
        syncRecordCounter();
    }

    private void syncRecordCounter() {
        int maxNumber = 0;
        for (AttendanceRecord record : attendanceRepository.findAll()) {
            String id = record.getRecordId();
            if (id != null && id.matches("REC\\d{1,9}")) {
                maxNumber = Math.max(maxNumber, Integer.parseInt(id.substring(3)));
            }
        }
        this.recordCounter = maxNumber + 1;
    }

    private boolean appendRecord(String personCode, String zoneId, AttendanceRecord.CheckType type)
            throws exception.AppError {
        String cleanPersonCode = personCode.trim();
        String cleanZoneId = zoneId.trim();

        AttendanceRecord lastRecord = attendanceRepository.findLastByPersonCode(cleanPersonCode);

        String recordId = String.format("REC%04d", recordCounter);
        String timestamp = LocalDateTime.now().format(util.DateHelper.TIMESTAMP_FORMATTER);
        AttendanceRecord newRecord = new AttendanceRecord(recordId, cleanPersonCode, cleanZoneId, timestamp, type);
        newRecord.assertCanFollow(lastRecord);
        recordCounter++;
        return attendanceRepository.save(newRecord);
    }

    public PersonnelService getPersonnelService() {
        return personnelService;
    }

    public ZoneService getZoneService() {
        return zoneService;
    }

    public boolean recordAttendance(String personCode, String zoneId, AttendanceRecord.CheckType type)
            throws exception.AppError, exception.NotFoundException {
        if (personCode == null || personCode.trim().isEmpty() || zoneId == null || zoneId.trim().isEmpty()
                || type == null) {
            throw new exception.DataValidationException("Person code, zone and action are required.");
        }
        model.Person person = personnelService.findPersonByCode(personCode.trim());
        if (type == AttendanceRecord.CheckType.CHECK_IN) {
            person.verifyCheckIn(java.time.LocalDate.now());
            zoneService.verifyAccess(zoneId.trim(), person);
        }
        return appendRecord(personCode, zoneId, type);
    }

    public boolean hasRecords(String personCode) {
        return attendanceRepository.findLastByPersonCode(personCode) != null;
    }

    public boolean hasRecordsForZone(String zoneId) {
        if (zoneId == null) {
            return false;
        }
        for (AttendanceRecord record : attendanceRepository.findAll()) {
            if (model.Zone.isSameId(record.getZoneId(), zoneId)) {
                return true;
            }
        }
        return false;
    }

    public List<AttendanceRecord> getAllAttendanceRecords() {
        return attendanceRepository.findAll();
    }

    public List<AttendanceRecord> getRecordsOf(String personCode) {
        List<AttendanceRecord> own = new java.util.ArrayList<>();
        for (AttendanceRecord record : attendanceRepository.findAll()) {
            if (record.getPersonCode().equalsIgnoreCase(personCode)) {
                own.add(record);
            }
        }
        return own;
    }

    public boolean loadFromCsv(String filePath) {
        boolean loaded = attendanceRepository.loadFromCsv(filePath);
        if (loaded) {
            syncRecordCounter();
        }
        return loaded;
    }

    public boolean saveToCsv(String filePath) {
        return attendanceRepository.saveToCsv(filePath);
    }
}
