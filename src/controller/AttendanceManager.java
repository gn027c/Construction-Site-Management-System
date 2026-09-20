package controller;

import model.AttendanceRecord;
import model.Incident;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * NHIỆM VỤ: Thành viên 4 (Nguyễn Tấn Lợi - SE211059)
 * MÔ TẢ:
 * - Module quản lý chu kỳ quẹt thẻ điểm danh và vòng đời hồ sơ sự cố an toàn (Attendance & Incident Lifecycle).
 * - Chịu trách nhiệm thực hiện các thuật toán: Quẹt thẻ FSM (chặn check-in kép), Quản lý xử lý sự cố.
 */
public class AttendanceManager {
    private final List<AttendanceRecord> attendanceList = new ArrayList<>();
    private final List<Incident> incidentList = new ArrayList<>();
    private int recordCounter = 1;

    public boolean processAttendance(String personCode, String zoneId, AttendanceRecord.CheckType type) {
        if (personCode == null || personCode.trim().isEmpty() || zoneId == null || zoneId.trim().isEmpty() || type == null) {
            return false;
        }

        String cleanPersonCode = personCode.trim();
        String cleanZoneId = zoneId.trim();

        AttendanceRecord lastRecord = null;
        for (int i = attendanceList.size() - 1; i >= 0; i--) {
            AttendanceRecord rec = attendanceList.get(i);
            if (rec.getPersonCode().equalsIgnoreCase(cleanPersonCode)) {
                lastRecord = rec;
                break;
            }
        }

        if (type == AttendanceRecord.CheckType.CHECK_IN) {
            if (lastRecord != null && lastRecord.getCheckType() == AttendanceRecord.CheckType.CHECK_IN) {
                return false;
            }
        } else if (type == AttendanceRecord.CheckType.CHECK_OUT) {
            if (lastRecord == null || lastRecord.getCheckType() == AttendanceRecord.CheckType.CHECK_OUT) {
                return false;
            }
        }

        String recordId = String.format("REC%04d", recordCounter++);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        AttendanceRecord newRecord = new AttendanceRecord(recordId, cleanPersonCode, cleanZoneId, timestamp, type);
        attendanceList.add(newRecord);
        return true;
    }

    public List<AttendanceRecord> getAllAttendanceRecords() {
        return new ArrayList<>(attendanceList);
    }

    public boolean reportIncident(Incident incident) {
        if (incident == null) {
            return false;
        }
        for (Incident inc : incidentList) {
            if (inc.getIncidentId().equalsIgnoreCase(incident.getIncidentId())) {
                return false;
            }
        }
        incidentList.add(incident);
        return true;
    }

    public boolean assignIncident(String incidentId, String assigneeCode) {
        if (incidentId == null || assigneeCode == null || assigneeCode.trim().isEmpty()) {
            return false;
        }
        for (Incident inc : incidentList) {
            if (inc.getIncidentId().equalsIgnoreCase(incidentId.trim())) {
                if (inc.getStatus() == Incident.IncidentStatus.RESOLVED) {
                    return false;
                }
                inc.setAssignedTo(assigneeCode.trim());
                inc.setStatus(Incident.IncidentStatus.ASSIGNED);
                return true;
            }
        }
        return false;
    }

    public boolean resolveIncident(String incidentId) {
        if (incidentId == null) {
            return false;
        }
        for (Incident inc : incidentList) {
            if (inc.getIncidentId().equalsIgnoreCase(incidentId.trim())) {
                inc.setStatus(Incident.IncidentStatus.RESOLVED);
                return true;
            }
        }
        return false;
    }

    public List<Incident> getAllIncidents() {
        return new ArrayList<>(incidentList);
    }
}
