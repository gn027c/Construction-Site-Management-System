# PHÂN CÔNG NHIỆM VỤ CHI TIẾT - WORKSHOP 5
> **Môn học**: PRO192 (Java OOP) - Lớp SE2115 - Nhóm 2 (Topic 7: SmartSite)  
> **Mục tiêu Workshop 5**: Tách biệt lớp lưu trữ tệp tin (Persistence Layer), tích hợp Java Collection Framework, tạo cấu trúc lưu trữ Mục 6.2 và Appendix (Mục 9).  
> **Hạn nộp**: Chủ Nhật, 01/11/2026 - 23:59  

---

## BẢNG PHÂN CÔNG NHIỆM VỤ THEO THÀNH VIÊN (CÂN BẰNG 25%)

### 👤 Thành viên 1: Huỳnh Nguyễn Hoàng Khang (Nhóm trưởng - SE201461)
- **Code**:
  - Thiết kế kiến trúc tổng thể lớp Persistence: Tạo generic interface hoặc chuẩn hóa phương thức File I/O.
  - Chuẩn hóa việc lưu trữ trong bộ nhớ tạm bằng Java Collection: áp dụng `LinkedHashMap<String, Person>` và `LinkedHashMap<String, Zone>` để tra cứu nhanh `O(1)`.
- **Report**:
  - Viết phần mở đầu Mục 6.2 (Tổng quan kiến trúc Persistence Layer và định dạng mã hóa UTF-8 BOM hỗ trợ hiển thị tiếng Việt trên Microsoft Excel).

---

### 👤 Thành viên 2: Trần Ngọc Anh Tuấn (Core Dev - SE201513)
- **Code**:
  - Cài đặt module lưu trữ nhân sự: Đọc/Ghi dữ liệu danh bạ ra file CSV (`persons.csv`) sử dụng `ArrayList<Person>`.
- **Report**:
  - Viết chi tiết cấu trúc định dạng file `persons.csv` trong Mục 6.2.
  - Chuẩn bị bộ dữ liệu mẫu Nhân sự (ít nhất 10 dòng mẫu chuẩn) đưa vào Appendix (Mục 9).

---

### 👤 Thành viên 3: Lê Tấn Thiên (Core Dev - SE201852)
- **Code**:
  - Cài đặt module lưu trữ khu vực: Đọc/Ghi dữ liệu phân vùng và danh sách nhân sự được cấp phép ra file CSV (`zones.csv`) sử dụng `Set<String>` và `HashMap`.
- **Report**:
  - Viết chi tiết cấu trúc định dạng file `zones.csv` trong Mục 6.2.
  - Chuẩn bị bộ dữ liệu mẫu Phân vùng (các zone bình thường và restricted) đưa vào Appendix (Mục 9).

---

### 👤 Thành viên 4: Nguyễn Tấn Lợi (Data Specialist - SE211059)
- **Code**:
  - Cài đặt module lưu trữ điểm danh & sự cố: Đọc/Ghi lịch sử quẹt thẻ (`attendance.csv`) và danh sách sự cố (`incidents.csv`) ra file CSV.
- **Report**:
  - Viết chi tiết cấu trúc định dạng file `attendance.csv` và `incidents.csv` trong Mục 6.2.
  - Chuẩn bị bộ dữ liệu mẫu Lịch sử điểm danh và Sự cố đưa vào Appendix (Mục 9).
