package controller;

/**
 * NHIỆM VỤ THỰC HIỆN - WORKSHOP 2 (CÂN BẰNG 4 THÀNH VIÊN):
 * 
 * 1. Thành viên 1: Huỳnh Nguyễn Hoàng Khang (SE201461)
 *    - Khởi tạo các cấu trúc lưu trữ danh sách trên bộ nhớ: personList, zoneList, attendanceList, incidentList.
 *    - Quản lý vòng đời khởi động hệ thống và kết nối tầng Controller với MenuView / InputHelper.
 * 
 * 2. Thành viên 2: Trần Ngọc Anh Tuấn (SE201513)
 *    - Cài đặt các hàm nghiệp vụ Nhân sự:
 *      + addPerson(Person p): Thêm mới nhân sự (kiểm tra chặn trùng mã).
 *      + findPersonByCode(String code): Tra cứu hồ sơ nhân sự theo mã.
 *      + updatePerson(String code, String newName, String newPhone): Cập nhật thông tin nhân sự.
 *      + deletePerson(String code): Xóa nhân sự theo mã.
 *      + getAllPersons(): Trả về danh sách toàn bộ nhân sự.
 * 
 * 3. Thành viên 3: Lê Tấn Thiên (SE201852)
 *    - Cài đặt các hàm nghiệp vụ Phân vùng & An toàn:
 *      + addZone(Zone z): Thêm khu vực thi công mới (kiểm tra chặn trùng mã zoneId).
 *      + findZoneById(String zoneId): Tra cứu khu vực theo mã.
 *      + grantZoneAccess(String zoneId, String personCode): Cấp quyền vào khu vực nguy hiểm.
 *      + revokeZoneAccess(String zoneId, String personCode): Thu hồi quyền vào khu vực hạn chế.
 *      + verifyAccess(String zoneId, String personCode): Kiểm tra quyền ra/vào qua đa hình checkAccess().
 *      + getAllZones(): Trả về danh sách toàn bộ khu vực.
 * 
 * 4. Thành viên 4: Nguyễn Tấn Lợi (SE211059)
 *    - Cài đặt các hàm nghiệp vụ Điểm danh & Sự cố:
 *      + processAttendance(String personCode, String zoneId, AttendanceRecord.CheckType type):
 *        Xử lý quẹt thẻ với thuật toán kiểm tra chu kỳ FSM (chặn Check-In 2 lần, chặn Check-Out khi chưa Check-In).
 *      + getAllAttendanceRecords(): Trả về toàn bộ lịch sử quẹt thẻ điểm danh.
 *      + reportIncident(Incident incident): Ghi nhận hồ sơ sự cố mới phát sinh.
 *      + assignIncident(String incidentId, String assigneeCode): Phân công người xử lý sự cố.
 *      + resolveIncident(String incidentId): Đóng sự cố đã được khắc phục.
 *      + getAllIncidents(): Trả về danh sách toàn bộ sự cố.
 */
public class SiteManagerController {

}
