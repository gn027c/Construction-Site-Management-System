# PHÂN CÔNG NHIỆM VỤ CHI TIẾT - WORKSHOP 2
> **Môn học**: PRO192 (Java OOP) - Lớp SE2115 - Nhóm 2 (Topic 7: SmartSite)  
> **Mục tiêu Workshop 2**: Khai báo các lớp quản lý (Manager / Controller classes) & Hoàn thành Mục 3 (Algorithm Design).  
> **Hạn nộp**: Chủ Nhật, 27/09/2026 - 23:59  

---

## BẢNG PHÂN CÔNG NHIỆM VỤ THEO THÀNH VIÊN (CÂN BẰNG 25%)

### 👤 Thành viên 1: Huỳnh Nguyễn Hoàng Khang (Nhóm trưởng - SE201461)
- **Code**:
  - Xây dựng khung điều phối trung tâm `SiteManagerController.java`.
  - Thiết lập luồng điều hướng Console Menu (`MenuView.java`) và xử lý nhập liệu (`InputHelper.java`).
- **Report (Mục 3)**:
  - Viết Mục 3.1: Kiến trúc điều phối hệ thống tổng thể và luồng điều khiển giữa Controller và View.

---

### 👤 Thành viên 2: Trần Ngọc Anh Tuấn (Core Dev - SE201513)
- **Code**:
  - Xây dựng các hàm nghiệp vụ quản lý Nhân sự trong Controller:
    - `registerPerson(Person p)`: Thêm nhân sự mới.
    - `findPersonByCode(String code)`: Tìm kiếm hồ sơ nhân sự theo mã.
    - `updatePerson(Person p)`: Cập nhật thông tin nhân sự.
- **Report (Mục 3)**:
  - Viết Mục 3.2: Thiết kế giải thuật tìm kiếm, thêm mới và xác thực hợp lệ hồ sơ nhân sự (kèm lưu đồ flowchart).

---

### 👤 Thành viên 3: Lê Tấn Thiên (Core Dev - SE201852)
- **Code**:
  - Xây dựng các hàm nghiệp vụ quản lý Phân vùng & An toàn:
    - `addZone(Zone z)`: Thêm khu vực thi công mới.
    - `findZoneById(String zoneId)`: Tra cứu thông tin khu vực.
    - `grantZoneAccess(String zoneId, String personCode)`: Cấp quyền vào khu vực hạn chế.
- **Report (Mục 3)**:
  - Viết Mục 3.3: Thiết kế giải thuật phân vùng an toàn và kiểm soát quyền vào khu vực nguy hiểm (kèm lưu đồ flowchart).

---

### 👤 Thành viên 4: Nguyễn Tấn Lợi (Data Specialist - SE211059)
- **Code**:
  - Xây dựng thuật toán quẹt thẻ và quản lý sự cố:
    - `processAttendance(String personCode, String zoneId, AttendanceType type)`: Kiểm tra chu kỳ vào/ra, chặn check-in 2 lần liên tiếp.
    - `reportIncident(Incident inc)` & `resolveIncident(String incidentId)`: Quản lý vòng đời sự cố.
- **Report (Mục 3)**:
  - Viết Mục 3.4: Thiết kế giải thuật kiểm tra chu kỳ điểm danh và xử lý sự cố an toàn (kèm lưu đồ flowchart).
