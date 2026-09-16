# PHÂN CÔNG NHIỆM VỤ CHI TIẾT - WORKSHOP 4
> **Mục tiêu Workshop 4**: Cài đặt kiểm tra ngoại lệ chặt chẽ (Exception Handling), bẫy lỗi console. Thiết kế ma trận kiểm thử chi tiết Mục 6.1 (Test Cases Matrix).

## Phân công thành viên:
- **Thành viên 1 (Khang - Leader)**: Xây dựng `ConsoleDispatcher.java` bọc khối try-catch tập trung (Global Exception Handling) và lớp tiện ích `InputHelper.java` chống trôi buffer và crash màn hình.
- **Thành viên 2 (Tuấn - Nhân sự)**: Tích hợp ngoại lệ `DuplicateCodeException.java` và `PersonNotFoundException.java` vào module nhân sự, bẫy lỗi trùng mã khi đăng ký.
- **Thành viên 3 (Thiên - Zone & Sự cố)**: Tích hợp ngoại lệ `InvalidZoneAccessException.java` khi nhân sự cố tình bước vào khu vực cấm không có chứng chỉ đạt chuẩn.
- **Thành viên 4 (Lợi - Data & QA)**: Thiết kế ma trận 15-20 Test Cases chi tiết ở Mục 6.1 (phân cấp Normal, Boundary, Exceptional) và cập nhật AI Audit Log (Entry 006, 007).
