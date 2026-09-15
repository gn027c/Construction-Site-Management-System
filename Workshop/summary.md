# TỔNG HỢP KẾ HOẠCH WORKSHOP & DỰ ÁN PRO192 (SMARTSITE - TOPIC 7)

> **Dự án**: SmartSite: AI-Integrated Construction Site Management System (Topic 7)  
> **Số lượng thành viên giả định**: Nhóm 4 người (chuẩn FPT PRO192: Nhóm trưởng + 3 thành viên).  
> **Quy ước đặt tên file chung**:  
> - Report: `<GiaiDoan>_<Nhom>_<Topic>.docx` / `.pdf` (Ví dụ: `Workshop1_Group1_Topic7.docx`)  
> - Code: `<GiaiDoan>_Code_<Nhom>_<Topic>.zip` (Ví dụ: `Workshop1_Code_Group1_Topic7.zip`)

---

## 1. Phân vai & Trách nhiệm thành viên (Đề xuất)

| Thành viên | Vai trò chính | Nhiệm vụ kỹ thuật trọng tâm | Phụ trách phần Report |
| :--- | :--- | :--- | :--- |
| **Thành viên 1** | **Nhóm trưởng (Team Leader)** | Quản lý tiến độ, kiến trúc tổng thể, menu console chính (`Main/App`), tích hợp dữ liệu, tổng hợp code & nộp bài. | Mục 1 (Introduction), Mục 5 (OOP Design & Class Diagram), Tổng hợp & Review Report |
| **Thành viên 2** | **Core Dev - Quản lý Nhân sự** | Lớp `Person` và các lớp con (`Worker`, `Contractor`, `Visitor`), module điểm danh `AttendanceRecord` & logic check-in/out. | Mục 2 (Requirement Analysis), Mục 3 (Algorithm Design: Check-in/out, validation) |
| **Thành viên 3** | **Core Dev - Khu vực & Sự cố** | Lớp `Zone`, `RestrictedZone`, `Incident`, các quy tắc an toàn, quản lý sự cố và phân công xử lý. | Mục 4 (Implementation of Functions), Mục 8 (Computational Thinking) |
| **Thành viên 4** | **Data & QA Specialist** | Lớp lưu trữ tập tin (`Persistence Layer`/File I/O), xử lý ngoại lệ (Exception Handling), xây dựng Test Cases và chạy nghiệm thu. | Mục 6 (Test Cases & Storage), Mục 7 (Experimental Results), Mục 9 (Appendix) |

---

## 2. Lộ trình Timeline chi tiết & Phân chia công việc

### Workshop 1
- **Hạn chót**: Đầu kỳ (theo thông báo)
- **Mục tiêu Code**: Khai báo các lớp cơ bản, Constructor, Getters/Setters, phương thức validation dữ liệu đầu vào.
- **Mục tiêu Report**: Hoàn thành Mục 1 (Introduction) & Mục 2 (Requirement Analysis & System Decomposition).
- **Phân công công việc**:
  - **Thành viên 1**: Khởi tạo cấu trúc project, viết lớp trừu tượng/cơ sở `Person`, viết Mục 1 (Mô tả dự án, thông tin nhóm).
  - **Thành viên 2**: Viết các thuộc tính, constructor, getter/setter, validation cho `Worker`, `Contractor`, `Visitor`; viết Mục 2.1 (Data Models).
  - **Thành viên 3**: Viết các thuộc tính, constructor, getter/setter, validation cho `Zone`, `RestrictedZone`, `Incident`; viết Mục 2.2 (System Functions).
  - **Thành viên 4**: Viết lớp `AttendanceRecord`, định nghĩa các quy chuẩn validate (mã code, định dạng ngày giờ, dữ liệu rỗng/âm).

---

### Workshop 2
- **Hạn chót**: Chủ Nhật, 27/09/2026 - 23:59
- **Mục tiêu Code**: Khai báo lớp quản lý (Manager / Controller classes).
- **Mục tiêu Report**: Hoàn thành Mục 3 (Algorithm Design).
- **Phân công công việc**:
  - **Thành viên 1**: Thiết kế khung `SiteManager` (quản trị tập trung điều phối các danh sách đối tượng), viết luồng menu điều hướng.
  - **Thành viên 2**: Xây dựng thuật toán và phương thức quản lý nhân sự (thêm, cập nhật, tìm kiếm người, check-in/out); vẽ lưu đồ / viết Mục 3 cho module nhân sự.
  - **Thành viên 3**: Xây dựng thuật toán và phương thức quản lý sự cố an toàn & phân vùng công trường; viết Mục 3 cho module sự cố và khu vực.
  - **Thành viên 4**: Xây dựng thuật toán tìm kiếm, lọc báo cáo điểm danh/sự cố theo tuần/tháng; rà soát thiết kế thuật toán mục 3.

---

### Workshop 3
- **Hạn chót**: Chủ Nhật, 04/10/2026 - 23:59
- **Mục tiêu Code**: Hoàn chỉnh kế thừa (Inheritance) và đa hình (Polymorphism) giữa các lớp.
- **Mục tiêu Report**: Hoàn thành Mục 4 (Implementation of Basic Functions) & Mục 5 (Object-Oriented Design - OOP / UML Class Diagram).
- **Phân công công việc**:
  - **Thành viên 1**: Tối ưu tính đa hình (override methods như `displayDetails()`, tính toán quyền hạn vào khu vực), vẽ sơ đồ UML Class Diagram hoàn chỉnh cho Mục 5.
  - **Thành viên 2**: Hoàn thiện tính kế thừa `Worker`, `Contractor`, `Visitor` từ `Person`; viết tài liệu code & giải thích cho Mục 4.1.
  - **Thành viên 3**: Hoàn thiện tính kế thừa `RestrictedZone` từ `Zone`, xử lý logic kiểm tra vi phạm khu vực cấm; viết tài liệu code & giải thích cho Mục 4.2.
  - **Thành viên 4**: Tích hợp các hàm kiểm tra đa hình khi duyệt danh sách; chuẩn bị tài liệu giải thích các nguyên lý OOP (Encapsulation, Inheritance, Polymorphism, Abstraction) trong Mục 5.

---

### Workshop 4
- **Hạn chót**: Chủ Nhật, 11/10/2026 - 23:59
- **Mục tiêu Code**: Hoàn chỉnh các class và cài đặt kiểm tra ngoại lệ chặt chẽ (Exception Handling).
- **Mục tiêu Report**: Tạo Test Cases chi tiết ở Mục 6.1 (Test Cases Matrix).
- **Phân công công việc**:
  - **Thành viên 1**: Xây dựng các Custom Exceptions (ví dụ: `InvalidZoneAccessException`, `PersonNotFoundException`, `DuplicateCodeException`), bắt lỗi toàn cục trong Console UI.
  - **Thành viên 2**: Bọc try-catch và kiểm tra validation cho module Nhân sự & Check-in/out (mã không tồn tại, check-in trùng lặp).
  - **Thành viên 3**: Bọc try-catch và kiểm tra validation cho module Sự cố & Phân vùng (gán sự cố cho người không hợp lệ, phân vùng sai cấp độ).
  - **Thành viên 4**: Thiết kế ma trận kiểm thử Mục 6.1 (ít nhất 10-15 test cases bao gồm Normal, Boundary, Exceptional cases) và đối chiếu với code.

---

### Workshop 5
- **Hạn chót**: Chủ Nhật, 01/11/2026 - 23:59
- **Mục tiêu Code**: Tách biệt lớp lưu trữ tệp tin (Persistence Layer / File I/O) và chuẩn hóa bằng Java Collection Framework (`ArrayList`, `HashMap`, `Set`).
- **Mục tiêu Report**: Cập nhật toàn bộ các mục đã làm trước đó; viết Mục 6.2 (Cấu trúc lưu trữ file) & Mục 9 (Appendix - Sample Data).
- **Phân công công việc**:
  - **Thành viên 1**: Thiết kế interface `DataPersistence` (hoặc DAO pattern), chuẩn hóa chuyển đổi toàn bộ cấu trúc dữ liệu sang Java Collections.
  - **Thành viên 2**: Viết logic đọc/ghi dữ liệu nhân sự và lịch sử điểm danh ra file văn bản/nhị phân (`persons.txt`, `attendance.txt`); viết mô tả cấu trúc lưu trữ cho Mục 6.2.
  - **Thành viên 3**: Viết logic đọc/ghi dữ liệu khu vực và sự cố an toàn (`zones.txt`, `incidents.txt`); xuất file báo cáo tổng hợp.
  - **Thành viên 4**: Chuẩn bị bộ dữ liệu mẫu (Sample data file) đưa vào Mục 9 (Appendix); rà soát đồng bộ toàn bộ nội dung Report từ Mục 1 đến Mục 6.

---

### Workshop 6
- **Hạn chót**: Chủ Nhật, 08/11/2026 - 23:59
- **Mục tiêu Code**: Hoàn chỉnh toàn bộ hệ thống, chạy thực tế ma trận kiểm thử (Test Cases Matrix) để loại bỏ mọi bug.
- **Mục tiêu Report**: Hoàn thành Mục 7 (Experimental Results - chụp màn hình kết quả chạy thực tế), Mục 8 (Computational Thinking) và hoàn chỉnh toàn bộ tài liệu Report.
- **Phân công công việc**:
  - **Thành viên 1**: Review toàn bộ mã nguồn, fix lỗi liên kết hệ thống, viết Mục 8 (Computational Thinking: phân rã bài toán, trừu tượng hóa, thuật toán).
  - **Thành viên 2 & 3**: Chạy demo từng chức năng theo vai trò, chụp ảnh màn hình kết quả chạy thực tế (Input/Output) cho Mục 7.
  - **Thành viên 4**: Thực thi toàn bộ Test Cases ở Mục 6.1, điền cột Actual Result, đánh giá PASS/FAIL và hoàn thiện file Report hoàn chỉnh.

---

### Báo cáo Cuối Kỳ (Final Submission)
- **Hạn chót**: Chủ Nhật, 08/11/2026 - 23:59
- **Cấu trúc gói nộp**:
  1. Thư mục `Document/`: Chứa file báo cáo `Final_Group1_Topic7.docx` (hoặc `.pdf`).
  2. Thư mục `Source/`: Chứa mã nguồn đóng gói `Final_Code_Group1_Topic7.zip`.
  3. Thư mục `Presentation/`: Chứa slide thuyết trình `Final_Slide_Group1_Topic7.pptx` (hoặc `.pdf`).
  4. Thư mục `AI_Audit_Log/`: Chứa file nhật ký tương tác AI của từng thành viên (optional).
- **Phân công công việc**:
  - **Thành viên 1**: Đóng gói source code chuẩn sạch, chuẩn bị slide thuyết trình (kiến trúc & OOP), kiểm duyệt gói nộp và đại diện nộp bài.
  - **Thành viên 2**: Soạn nội dung slide phần Quản lý Nhân sự & Luồng Điểm danh, trích xuất log AI cá nhân.
  - **Thành viên 3**: Soạn nội dung slide phần Quản lý An toàn, Sự cố & Phân vùng, trích xuất log AI cá nhân.
  - **Thành viên 4**: Định dạng toàn bộ tài liệu Document cuối kỳ theo đúng template chuẩn, tổng hợp file AI Audit Log của cả nhóm.
