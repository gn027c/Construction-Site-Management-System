package service;

import model.AttendanceRecord;
import repository.AttendanceRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * TẦNG NGHIỆP VỤ (SERVICE LAYER)
 * PHỤ TRÁCH: Thành viên 4 (Nguyễn Tấn Lợi - SE211059)
 * MÔ TẢ: Xử lý chu kỳ quẹt thẻ điểm danh, thuật toán FSM và phối hợp nghiệp vụ liên module.
 */
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
    }

    public boolean processAttendance(String personCode, String zoneId, AttendanceRecord.CheckType type) {
        if (personCode == null || personCode.trim().isEmpty() || zoneId == null || zoneId.trim().isEmpty()
                || type == null) {
            return false;
        }

        String cleanPersonCode = personCode.trim();
        String cleanZoneId = zoneId.trim();

        AttendanceRecord lastRecord = attendanceRepository.findLastByPersonCode(cleanPersonCode);

        // FSM: Chặn CHECK_IN khi đang ở trong; Chặn CHECK_OUT khi chưa vào hoặc sai khu vực
        if (type == AttendanceRecord.CheckType.CHECK_IN) {
            if (lastRecord != null && lastRecord.getCheckType() == AttendanceRecord.CheckType.CHECK_IN) {
                return false;
            }
        } else if (type == AttendanceRecord.CheckType.CHECK_OUT) {
            if (lastRecord == null || lastRecord.getCheckType() == AttendanceRecord.CheckType.CHECK_OUT
                    || !lastRecord.getZoneId().equalsIgnoreCase(cleanZoneId)) {
                return false;
            }
        }

        String recordId = String.format("REC%04d", recordCounter++);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        AttendanceRecord newRecord = new AttendanceRecord(recordId, cleanPersonCode, cleanZoneId, timestamp, type);
        return attendanceRepository.save(newRecord);
    }

    public PersonnelService getPersonnelService() {
        return personnelService;
    }

    public ZoneService getZoneService() {
        return zoneService;
    }

    public boolean recordAttendance(String personCode, String zoneId, AttendanceRecord.CheckType type) {
        if (personCode == null || personCode.trim().isEmpty() || zoneId == null || zoneId.trim().isEmpty() || type == null) {
            System.out.println("Error: Invalid attendance parameters!");
            return false;
        }

        // 1. Kiểm tra tồn tại nhân sự
        model.Person person = personnelService.findPersonByCode(personCode.trim());
        if (person == null) {
            System.out.println("Error: Person with code '" + personCode + "' not found.");
            return false;
        }

        // 2. Nếu CHECK_IN, kiểm tra quyền truy cập vào khu vực qua verifyAccess (đa hình)
        if (type == AttendanceRecord.CheckType.CHECK_IN) {
            if (!zoneService.verifyAccess(zoneId.trim(), person)) {
                System.out.println("Access Denied: Person '" + person.getName() + "' is not authorized for zone '" + zoneId + "'.");
                return false;
            }
        }

        // 3. Cập nhật trạng thái máy FSM (chặn vào trùng hoặc ra sai khu vực)
        boolean success = processAttendance(personCode, zoneId, type);
        if (!success) {
            System.out.println("Error: Invalid attendance sequence (FSM violation).");
            return false;
        }

        // 4. In thông tin chi tiết nhân sự khi thành công (đa hình displayDetails)
        System.out.println("Attendance recorded successfully: " + type + " at zone " + zoneId);
        person.displayDetails();
        return true;
    }

    public List<AttendanceRecord> getAllAttendanceRecords() {
        return attendanceRepository.findAll();
    }

    public void displayAllAttendanceRecords() {
        List<AttendanceRecord> list = attendanceRepository.findAll();
        if (list.isEmpty()) {
            System.out.println("No attendance records yet.");
            return;
        }
        for (AttendanceRecord r : list) {
            System.out.println(r);
        }
    }
}
