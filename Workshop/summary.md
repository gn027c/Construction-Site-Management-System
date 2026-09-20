# TỔNG HỢP KẾ HOẠCH WORKSHOP & DỰ ÁN PRO192 (SMARTSITE - TOPIC 7)

> **Dự án**: SmartSite: AI-Integrated Construction Site Management System (Topic 7)  
> **Lớp**: SE2115 - Nhóm 2  
> **Giảng viên hướng dẫn**: ThS. Nguyễn Tấn Phúc  
> **Số lượng thành viên**: 4 người (Nhóm trưởng + 3 thành viên).  
> **Quy ước đặt tên file nộp bài**:  
> - Báo cáo: `Workshop<N>_Group2_Topic7.docx` / `.pdf`  
> - Mã nguồn: `Workshop<N>_Code_Group2_Topic7.zip`  

---

## 1. MA TRẬN PHÂN VAI & PHÂN CÔNG TỔNG QUAN (4 THÀNH VIÊN - CÂN BẰNG 25%)

| Thành viên | Vai trò | Trách nhiệm Kỹ thuật (Code xuyên suốt 6 tuần) | Trách nhiệm Báo cáo (Report Word) |
| :--- | :--- | :--- | :--- |
| **1. Huỳnh Nguyễn Hoàng Khang** (SE201461) | **Nhóm trưởng (Architecture & Lead)** | - Model gốc: `Person.java` (abstract), `Role.java` (enum), `Worker.java` (mẫu kế thừa).<br>- Điều phối: `SiteManagerController.java`, Menu UI console.<br>- Đa hình toàn cục & Custom Exception.<br>- Tích hợp hệ thống, đóng gói mã nguồn `.zip`. | - Mục 1 (Introduction & Team Info).<br>- Mục 3.1 (System Architecture & Control Flow).<br>- Mục 5 (OOP Design & Class Diagram UML).<br>- Mục 8 (Computational Thinking).<br>- Tổng hợp, soát lỗi & nộp bài. |
| **2. Trần Ngọc Anh Tuấn** (SE201513) | **Core Dev - Quản lý Nhân sự (Personnel)** | - Model: `Contractor.java`, `Visitor.java` (kế thừa `Person`).<br>- Nghiệp vụ Nhân sự: CRUD danh bạ, tìm kiếm `findPersonByCode()`.<br>- Validation & Exception nhân sự (`InvalidPhoneNumber`, `DuplicateCode`).<br>- Lưu trữ: Đọc/Ghi `persons.csv` dùng Java Collection (`ArrayList`). | - Mục 2.1 (Data Models: Contractor, Visitor, Worker).<br>- Mục 3.2 (Thuật toán tìm kiếm & cập nhật nhân sự).<br>- Mục 4.1 (Cài đặt phân cấp kế thừa Person & Encapsulation).<br>- Mục 6.1 (Test Cases module Nhân sự).<br>- Mục 6.2 (Cấu trúc bảng dữ liệu `persons.csv`) & Appendix. |
| **3. Lê Tấn Thiên** (SE201852) | **Core Dev - Khu vực & An toàn (Zone & Safety)** | - Model: `Zone.java`, `RestrictedZone.java` (kế thừa `Zone`).<br>- Nghiệp vụ Phân vùng: Thêm khu vực, quản lý danh sách cấp phép vùng nguy hiểm.<br>- Đa hình động: `zone.checkAccess(person)` kiểm tra cấp độ an toàn.<br>- Validation & Exception khu vực (`RestrictedZoneViolation`).<br>- Lưu trữ: Đọc/Ghi `zones.csv` dùng `HashMap`, `Set`. | - Mục 2.1 (Data Models: Zone, RestrictedZone).<br>- Mục 2.2 (System Functions: Phân vùng công trường).<br>- Mục 3.3 (Thuật toán kiểm soát truy cập phân vùng).<br>- Mục 4.2 (Cài đặt đa hình kiểm tra quyền truy cập).<br>- Mục 6.1 (Test Cases module Phân vùng & Cấp phép).<br>- Mục 6.2 (Cấu trúc file `zones.csv`) & Appendix. |
| **4. Nguyễn Tấn Lợi** (SE211059) | **Data Specialist - Điểm danh & Sự cố (Attendance & QA)** | - Model: `AttendanceRecord.java`, `Incident.java`, các Enum liên quan.<br>- Nghiệp vụ Điểm danh: Thuật toán quẹt thẻ `processAttendance()` (chặn check-in 2 lần, kiểm tra chu kỳ vào/ra).<br>- Nghiệp vụ Sự cố: Khai báo sự cố, phân công xử lý, dọn dẹp sự cố đã giải quyết.<br>- Validation & Exception điểm danh (`AttendanceOrderException`).<br>- Lưu trữ: Đọc/Ghi `attendance.csv`, `incidents.csv`. | - Mục 2.1 (Data Models: AttendanceRecord, Incident).<br>- Mục 2.2 (System Functions: Điểm danh & Quản lý sự cố).<br>- Mục 3.4 (Thuật toán chu kỳ điểm danh & vòng đời sự cố).<br>- Mục 6.1 (Tổng hợp Ma trận Test Cases toàn hệ thống).<br>- Mục 6.2 (Cấu trúc file `attendance.csv`, `incidents.csv`) & Appendix.<br>- Mục 7 (Thực thi Test Cases & Đánh giá kết quả Pass/Fail). |

---

## 2. LỘ TRÌNH TIMELINE CHI TIẾT TỪ WORKSHOP 1 ĐẾN WORKSHOP 6

### 📌 Workshop 1: Entity Classes, Constructors, Getters/Setters & Validation
- **Hạn nộp**: Đầu kỳ
- **Yêu cầu**: Khai báo các lớp thực thể cơ bản, Constructor, Getters/Setters, Validation dữ liệu đầu vào.
- **Phân công chi tiết**:
  - **Huỳnh Nguyễn Hoàng Khang**: Khởi tạo project; viết `Person.java` (abstract), `Role.java` (enum) và `Worker.java` (class mẫu kế thừa `Person`); viết Mục 1 của Report.
  - **Trần Ngọc Anh Tuấn**: Viết `Contractor.java` và `Visitor.java` (kế thừa `Person`); viết Mục 2.1 phần Nhân sự.
  - **Lê Tấn Thiên**: Viết `Zone.java` và `RestrictedZone.java` (kế thừa `Zone`); viết Mục 2.1 & 2.2 phần Phân vùng.
  - **Nguyễn Tấn Lợi**: Viết `AttendanceRecord.java` và `Incident.java` kèm các Enum trạng thái; viết Mục 2.1 & 2.2 phần Điểm danh và Sự cố.

---

### 📌 Workshop 2: Manager & Controller Classes
- **Hạn nộp**: Chủ Nhật, 27/09/2026 - 23:59
- **Yêu cầu**: Khai báo lớp quản lý, hoàn thành Mục 3 (Algorithm Design) trong Report.
- **Phân công chi tiết**:
  - **Huỳnh Nguyễn Hoàng Khang**: Xây dựng khung điều phối trung tâm `SiteManagerController.java`, điều hướng Console Menu; viết Mục 3.1 (Kiến trúc hệ thống và luồng điều khiển).
  - **Trần Ngọc Anh Tuấn**: Cài đặt các hàm quản lý nhân sự (`addPerson`, `updatePerson`, `findPersonByCode`, `deletePerson`); viết Mục 3.2 (Thuật toán tìm kiếm và xác thực nhân sự).
  - **Lê Tấn Thiên**: Cài đặt các hàm quản lý khu vực (`addZone`, `findZoneById`, cấp phép vào khu vực nguy hiểm); viết Mục 3.3 (Thuật toán phân vùng và kiểm soát ra vào).
  - **Nguyễn Tấn Lợi**: Cài đặt thuật toán quẹt thẻ `processAttendance` (kiểm tra chu kỳ vào/ra) và quản lý sự cố (`reportIncident`, `resolveIncident`); viết Mục 3.4 (Thuật toán điểm danh và xử lý sự cố).

---

### 📌 Workshop 3: Inheritance & Polymorphism
- **Hạn nộp**: Chủ Nhật, 04/10/2026 - 23:59
- **Yêu cầu**: Hoàn chỉnh các class liên quan đến kế thừa và đa hình, hoàn thành Mục 4 và Mục 5 trong Report.
- **Phân công chi tiết**:
  - **Huỳnh Nguyễn Hoàng Khang**: Chuẩn hóa đa hình toàn cục qua các phương thức trừu tượng `displayDetails()`, `toCsvLine()`; vẽ Sơ đồ UML Class Diagram hoàn chỉnh cho Mục 5.
  - **Trần Ngọc Anh Tuấn**: Hoàn thiện tính kế thừa và ghi đè phương thức hiển thị/xuất file cho `Worker`, `Contractor`, `Visitor`; viết Mục 4.1.
  - **Lê Tấn Thiên**: Hoàn thiện đa hình động `checkAccess(Person p)` giữa `Zone` và `RestrictedZone`; viết Mục 4.2.
  - **Nguyễn Tấn Lợi**: Hiện thực hóa đa hình phân loại xử lý danh sách sự cố theo mức độ nghiêm trọng; viết phần phân tích các nguyên lý OOP (Abstraction, Encapsulation, Inheritance, Polymorphism) cho Mục 5.

---

### 📌 Workshop 4: Exception Handling & Test Cases Matrix
- **Hạn nộp**: Chủ Nhật, 11/10/2026 - 23:59
- **Yêu cầu**: Hoàn chỉnh các class và kiểm tra ngoại lệ chặt chẽ, tạo Ma trận Test Cases trong Mục 6.1 của Report.
- **Phân công chi tiết**:
  - **Huỳnh Nguyễn Hoàng Khang**: Xây dựng hệ thống Custom Exceptions cơ sở (`SiteManagementException`) và bộ bắt lỗi tập trung (Global Exception Handler); thiết kế kịch bản Exceptional Test Cases trong Mục 6.1.
  - **Trần Ngọc Anh Tuấn**: Bọc Try-Catch và kiểm tra ngoại lệ dữ liệu nhân sự (`DuplicateCodeException`, `InvalidPhoneNumberException`); thiết kế 5-7 Test Cases nhân sự cho Mục 6.1.
  - **Lê Tấn Thiên**: Bọc Try-Catch và kiểm tra ngoại lệ phân vùng (`RestrictedZoneViolationException`); thiết kế 5-7 Test Cases kiểm soát khu vực cho Mục 6.1.
  - **Nguyễn Tấn Lợi**: Bọc Try-Catch và kiểm tra ngoại lệ điểm danh (`AttendanceOrderException`); tổng hợp và định dạng bảng Ma trận Test Cases đầy đủ cho Mục 6.1.

---

### 📌 Workshop 5: Persistence Layer & Java Collection Framework
- **Hạn nộp**: Chủ Nhật, 01/11/2026 - 23:59
- **Yêu cầu**: Tách biệt lớp lưu trữ tệp tin (Persistence Layer), tích hợp Java Collection Framework, cập nhật các mục trước, viết Mục 6.2 và Appendix.
- **Phân công chi tiết**:
  - **Huỳnh Nguyễn Hoàng Khang**: Thiết kế interface lưu trữ tổng quát (`IRepository`), chuẩn hóa Collection Framework toàn hệ thống (`LinkedHashMap` tra cứu nhanh `O(1)`); viết Mục 6.2 tổng quan kiến trúc lưu trữ.
  - **Trần Ngọc Anh Tuấn**: Cài đặt đọc/ghi file `persons.csv` dùng `ArrayList<Person>`; viết đặc tả cấu trúc `persons.csv` ở Mục 6.2 và chuẩn bị Sample Data nhân sự cho Mục 9 (Appendix).
  - **Lê Tấn Thiên**: Cài đặt đọc/ghi file `zones.csv` dùng `HashMap` và `Set<String>`; viết đặc tả cấu trúc `zones.csv` ở Mục 6.2 và chuẩn bị Sample Data khu vực cho Mục 9 (Appendix).
  - **Nguyễn Tấn Lợi**: Cài đặt đọc/ghi file `attendance.csv` và `incidents.csv`; viết đặc tả cấu trúc file ở Mục 6.2 và chuẩn bị Sample Data lịch sử điểm danh cho Mục 9 (Appendix).

---

### 📌 Workshop 6 & Final Submission: Complete System & Test Execution
- **Hạn nộp**: Chủ Nhật, 08/11/2026 - 23:59
- **Yêu cầu**: Hoàn chỉnh toàn bộ chương trình, chạy thực tế ma trận kiểm thử, chụp ảnh minh chứng, hoàn thiện toàn bộ Report.
- **Phân công chi tiết**:
  - **Huỳnh Nguyễn Hoàng Khang**: Review toàn bộ mã nguồn, fix lỗi liên kết, viết Mục 8 (Computational Thinking), tổng hợp toàn bộ file Report và đóng gói gói nộp `.zip`.
  - **Trần Ngọc Anh Tuấn**: Chạy demo thực tế các chức năng Quản lý Nhân sự, chụp ảnh màn hình Input/Output đưa vào Mục 7.
  - **Lê Tấn Thiên**: Chạy demo thực tế các chức năng Phân vùng công trường & Cảnh báo an toàn, chụp ảnh màn hình đưa vào Mục 7.
  - **Nguyễn Tấn Lợi**: Chạy thực tế toàn bộ Ma trận Test Cases Mục 6.1, điền cột Actual Result, xác nhận trạng thái Pass/Fail và hoàn thiện Mục 7.
