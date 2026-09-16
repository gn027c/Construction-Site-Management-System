# PHÂN CÔNG NHIỆM VỤ CHI TIẾT - WORKSHOP 5
> **Mục tiêu Workshop 5**: Tách biệt lớp lưu trữ tệp tin (Persistence Layer / CSV I/O) và tối ưu hóa bằng Java Collections Framework. Hoàn thành Mục 6.2 (Storage Structure) & Mục 9 (Appendix).

## Phân công thành viên:
- **Thành viên 1 (Khang - Leader)**: Chuẩn hóa toàn bộ cấu trúc dữ liệu sang `LinkedHashMap<String, Person>` đạt tốc độ tra cứu $O(1)$ theo mã và bảo toàn thứ tự hiển thị.
- **Thành viên 2 (Tuấn - Nhân sự)**: Hiện thực Factory Method Pattern (`PersonFactory.java`) kết hợp cột định danh kiểu (`Discriminator Column`) để khôi phục đúng kiểu đối tượng con (`Worker`, `Contractor`) khi nạp file `persons.csv`.
- **Thành viên 3 (Thiên - Zone & Sự cố)**: Viết logic đọc và lưu trữ danh sách khu vực (`zones.csv`) và lịch sử sự cố an toàn (`incidents.csv`).
- **Thành viên 4 (Lợi - Data & QA)**: Xây dựng lớp `PersonFileRepository.java`, viết bộ xử lý Regex RFC 4180 phân tách dấu phẩy an toàn; chuẩn bị bộ dữ liệu mẫu cho Mục 9; cập nhật AI Audit Log (Entry 008, 009, 010).
