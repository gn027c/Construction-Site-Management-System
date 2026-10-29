package service;

import model.AttendanceRecord;
import repository.AttendanceRepository;
import java.time.LocalDateTime;
import java.util.List;
import util.DateTimeHelper;

/**
 * TẦNG NGHIỆP VỤ (SERVICE LAYER)
 * PHỤ TRÁCH: Thành viên 4 (Nguyễn Tấn Lợi - SE211059)
 * MÔ TẢ: Tra cứu nhân sự/khu vực, gọi luật chuyển trạng thái của AttendanceRecord và lưu bản ghi.
 */
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final PersonnelService personnelService;
    private final ZoneService zoneService;
    private long recordCounter = 1;

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

    /**
     * Đặt recordCounter = (số REC lớn nhất đang có trong repository) + 1.
     * Không dùng size() + 1: nếu có bản ghi bị xóa thì size() có thể trùng mã đang tồn tại.
     * Bản ghi có mã không theo dạng REC#### (ví dụ ATT-001 trong dữ liệu mẫu) được bỏ qua.
     */
    private void syncRecordCounter() {
        long maxNumber = 0;
        for (AttendanceRecord record : attendanceRepository.findAll()) {
            String id = record.getRecordId();
            if (id != null && id.matches("REC\\d{1,18}")) {
                maxNumber = Math.max(maxNumber, Long.parseLong(id.substring(3)));
            }
        }
        this.recordCounter = maxNumber + 1;
    }

    public boolean processAttendance(String personCode, String zoneId, AttendanceRecord.CheckType type)
            throws exception.AttendanceOrderException {
        if (personCode == null || personCode.trim().isEmpty() || zoneId == null || zoneId.trim().isEmpty()
                || type == null) {
            return false;
        }

        String cleanPersonCode = personCode.trim();
        String cleanZoneId = zoneId.trim();
        model.Person person = personnelService.findPersonByCode(cleanPersonCode);
        zoneService.findZoneById(cleanZoneId);
        if (type == AttendanceRecord.CheckType.CHECK_IN) zoneService.verifyAccess(cleanZoneId, person);


        AttendanceRecord lastRecord = attendanceRepository.findLastByPersonCode(cleanPersonCode);

        String recordId = String.format("REC%04d", recordCounter);
        String timestamp = DateTimeHelper.formatTimestamp(LocalDateTime.now());
        AttendanceRecord newRecord = new AttendanceRecord(recordId, cleanPersonCode, cleanZoneId, timestamp, type);
        newRecord.assertCanFollow(lastRecord);
        boolean saved = attendanceRepository.save(newRecord);
        if (saved) recordCounter++;
        return saved;
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
}
