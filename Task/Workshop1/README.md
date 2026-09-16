# PHÂN CÔNG NHIỆM VỤ CHI TIẾT - WORKSHOP 1
> **Môn học**: PRO192 (Java OOP) - Lớp SE2115 - Nhóm 2 (Topic 7: SmartSite)  
> **Mục tiêu Workshop 1**: Khai báo các lớp thực thể cơ bản, Constructor, Getters/Setters, Validation dữ liệu đầu vào. Hoàn thành Mục 1 (Introduction) & Mục 2 (Requirements Analysis).

---

## 1. BẢNG PHÂN CÔNG THEO THÀNH VIÊN

### 👤 Thành viên 1: Huỳnh Nguyễn Hoàng Khang (Nhóm trưởng - SE201461)
- **Nhiệm vụ Code**:
  - Thiết lập cấu trúc thư mục dự án chuẩn: `src/model/`.
  - Khai báo lớp trừu tượng cơ sở `Person.java`:
    - Các thuộc tính `protected`: `id`, `code`, `name`, `phoneNumber`, `role`.
    - Khởi tạo Constructor mặc định và Constructor đầy đủ tham số.
    - Cài đặt đầy đủ Getters/Setters kèm validate cơ bản (mã, tên không rỗng).
    - Khai báo phương thức trừu tượng `public abstract void displayDetails();`.
  - Định nghĩa Enum `Role.java` (WORKER, CONTRACTOR, VISITOR, SAFETY_OFFICER, SITE_MANAGER).
- **Nhiệm vụ Report**:
  - Hoàn thành **Mục 1.1 (Project Description)**: Mô tả tổng quan đề tài SmartSite, bối cảnh công trường xây dựng, mục tiêu tối giản trên Console CLI.
  - Hoàn thành **Mục 1.2 (Project Team)**: Lập bảng thông tin thành viên nhóm và phân chia vai trò.

---

### 👤 Thành viên 2: Trần Ngọc Anh Tuấn (Core Dev - Nhân sự - SE201513)
- **Nhiệm vụ Code**:
  - Viết các lớp con kế thừa từ `Person`:
    - `Worker.java` (extends Person): Các thuộc tính `trade` (nghề), `safetyCertId` (chứng chỉ an toàn), `assignedTeam` (tổ/đội). Override phương thức `displayDetails()`.
    - `Contractor.java` (extends Person): Các thuộc tính `companyName` (công ty thầu), `contractId` (số hợp đồng), `representativeTitle` (chức danh). Override phương thức `displayDetails()`.
    - `Visitor.java` (extends Person): Các thuộc tính `purpose` (mục đích), `hostPersonName` (người đón tiếp), `visitDate` (ngày vào). Override phương thức `displayDetails()`.
- **Nhiệm vụ Report**:
  - Hoàn thành **Mục 2.1 (Data Models)**: Viết bảng đặc tả thuộc tính, kiểu dữ liệu, phạm vi truy cập (Encapsulation) cho cụm nhân sự (`Person`, `Worker`, `Contractor`, `Visitor`).

---

### 👤 Thành viên 3: Lê Tấn Thiên (Core Dev - Khu vực & Sự cố - SE201852)
- **Nhiệm vụ Code**:
  - Khai báo lớp khu vực `Zone.java`:
    - Thuộc tính `protected`: `zoneId`, `zoneName`, `description`.
    - Constructor, Getters/Setters.
    - Phương thức cơ sở: `public boolean checkAccess(Person person) { return person != null; }`.
  - Khai báo lớp thực thể sự cố `Incident.java`:
    - Thuộc tính: `incidentId`, `title`, `description`, `severity`, `status`, `reportedBy`, `assignedTo`.
    - Constructor, Getters/Setters.
- **Nhiệm vụ Report**:
  - Hoàn thành **Mục 2.2 (System Functions & User Stories)**: Viết các User Stories theo chuẩn môn học cho Cán bộ An toàn và Quản lý công trường.

---

### 👤 Thành viên 4: Nguyễn Tấn Lợi (Data Specialist & QA - SE211059)
- **Nhiệm vụ Code**:
  - Khai báo lớp bản ghi điểm danh `AttendanceRecord.java`:
    - Thuộc tính: `recordId`, `personCode`, `zoneId`, `timestamp`, `type`.
    - Định nghĩa Enum `RecordType.java` (`CHECK_IN`, `CHECK_OUT`).
  - Viết lớp kiểm thử độc lập `MainTest.java` (hoặc `Workshop1Demo.java`): Khởi tạo đối tượng `Worker`, `Contractor`, `Visitor`, `Zone` và chạy thử `displayDetails()` để xác nhận không lỗi biên dịch.
- **Nhiệm vụ Report & QA**:
  - Kiểm tra tính hợp lệ dữ liệu (validation rules): Kiểm tra mã định danh, số điện thoại, ngày tháng không rỗng.
  - Phụ trách lập và cập nhật **AI Audit Log (Workshop 1)**: Điền các Core Prompts về Decomposition (Entry 001) và Abstraction (Entry 002).
