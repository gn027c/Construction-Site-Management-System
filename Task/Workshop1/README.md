# PHÂN CÔNG NHIỆM VỤ CHI TIẾT - WORKSHOP 1
> **Môn học**: PRO192 (Java OOP) - Lớp SE2115 - Nhóm 2 (Topic 7: SmartSite)  
> **Mục tiêu Workshop 1**: Khai báo các lớp thực thể cơ bản, Constructor, Getters/Setters, Validation dữ liệu đầu vào.

---

## BẢNG PHÂN CÔNG NHIỆM VỤ THEO THÀNH VIÊN (CÂN BẰNG 25%)

### 👤 Thành viên 1: Huỳnh Nguyễn Hoàng Khang (Nhóm trưởng - SE201461)
- **Nhánh làm việc**: `khanghnh-core-person`
- **Nhiệm vụ Code**:
  - Khởi tạo cấu trúc dự án: `src/model/`.
  - Khai báo lớp trừu tượng cơ sở `Person.java`:
    - Thuộc tính: `protected String id`, `protected String code`, `protected String name`, `protected String phoneNumber`, `protected Role role`.
    - Constructor mặc định và Constructor đầy đủ tham số.
    - Getters/Setters kèm kiểm tra dữ liệu cơ bản.
    - Phương thức trừu tượng: `public abstract void displayDetails();` và `public abstract String toCsvLine();`.
  - Định nghĩa Enum `Role.java`: `WORKER`, `CONTRACTOR`, `VISITOR`, `SAFETY_OFFICER`, `SITE_MANAGER`.
  - Khai báo lớp con mẫu `Worker.java` (extends `Person`):
    - Thuộc tính riêng: `private String trade`, `private String safetyCertId`, `private String assignedTeam`.
    - Constructor gọi `super(...)` từ `Person`.
    - Getters/Setters, ghi đè `displayDetails()` và `toCsvLine()`.
- **Nhiệm vụ Report**: Viết Mục 1 (Introduction & Thông tin nhóm) trong file Word.

---

### 👤 Thành viên 2: Trần Ngọc Anh Tuấn (Core Dev - SE201513)
- **Nhánh làm việc**: `tuantna-personnel-subclasses`
- **Nhiệm vụ Code**:
  - Khai báo lớp con `Contractor.java` (extends `Person`):
    - Thuộc tính riêng: `private String companyName`, `private String contractId`.
    - Constructor mặc định và Constructor gọi `super(...)` từ `Person`.
    - Getters/Setters, ghi đè `displayDetails()` và `toCsvLine()`.
  - Khai báo lớp con `Visitor.java` (extends `Person`):
    - Thuộc tính riêng: `private String purpose`, `private String hostPersonName`, `private String entryDate`.
    - Constructor mặc định và Constructor gọi `super(...)` từ `Person`.
    - Getters/Setters, ghi đè `displayDetails()` và `toCsvLine()`.
- **Nhiệm vụ Report**: Viết Mục 2.1 (Đặc tả Data Models phần Nhân sự: Contractor, Visitor, Worker) trong file Word.

---

### 👤 Thành viên 3: Lê Tấn Thiên (Core Dev - SE201852)
- **Nhánh làm việc**: `thientt-zone-incident`
- **Nhiệm vụ Code**:
  - Khai báo lớp khu vực `Zone.java`:
    - Thuộc tính: `protected String zoneId`, `protected String zoneName`.
    - Constructor, Getters/Setters và phương thức `checkAccess(Person person)`.
  - Khai báo lớp con khu vực nguy hiểm `RestrictedZone.java` (extends `Zone`):
    - Thuộc tính riêng: `private String requiredSafetyLevel`, `private Set<String> allowedPersonCodes`.
    - Constructor gọi `super(...)`, Getters/Setters, ghi đè `checkAccess(Person person)`.
- **Nhiệm vụ Report**: Viết Mục 2.1 (Data Models: Zone, RestrictedZone) và Mục 2.2 (System Functions phần Khu vực) trong file Word.

---

### 👤 Thành viên 4: Nguyễn Tấn Lợi (Data Specialist - SE211059)
- **Nhánh làm việc**: `loint-attendance-record`
- **Nhiệm vụ Code**:
  - Khai báo lớp bản ghi điểm danh `AttendanceRecord.java`:
    - Thuộc tính: `private String recordId`, `private String personCode`, `private String zoneId`, `private String timestamp`, `private AttendanceType type`.
    - Enum `AttendanceType`: `CHECK_IN`, `CHECK_OUT`.
    - Constructor, Getters/Setters và phương thức xuất chuỗi CSV `toCsvLine()`.
  - Khai báo lớp hồ sơ sự cố `Incident.java`:
    - Thuộc tính: `private String incidentId`, `private String title`, `private String description`, `private IncidentSeverity severity`, `private IncidentStatus status`, `private String assignedTo`.
    - Các Enum: `IncidentSeverity` (LOW, MEDIUM, HIGH, CRITICAL), `IncidentStatus` (OPEN, ASSIGNED, RESOLVED).
    - Constructor, Getters/Setters.
- **Nhiệm vụ Report**: Viết Mục 2.1 (Data Models: AttendanceRecord, Incident) và Mục 2.2 (System Functions phần Điểm danh/Sự cố) trong file Word.
