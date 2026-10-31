# Thuật toán và thư viện trong SmartSite

Quyết định cho WS6: giữ thuật toán nghiệp vụ trong Service, tách phần lưu
file vào Repository/CsvHelper, dùng Java SE 8 cho CSV và I/O. Không thêm một
package `algorithm` chỉ để chuyển nguyên phương thức sang file khác.

## Chọn cách tổ chức theo trách nhiệm

| Thành phần | Vị trí hiện tại | Lý do |
| --- | --- | --- |
| Chu kỳ vào/ra | AttendanceRecord.assertCanFollow; AttendanceService điều phối | Model kiểm tra quy tắc, Service tìm nhân sự/khu vực và bản ghi trước rồi lưu |
| OPEN → ASSIGNED → RESOLVED | Incident.assignTo/resolve | Model tự giữ vòng đời; Service tìm sự cố và kiểm tra nhân sự tồn tại |
| Kiểm tra quyền vào khu | Zone/RestrictedZone.accessDenialReason, Person.hasValidSafetyCredential, SafetyLevel.covers | Đa hình: khu thường nhận mọi người; khu hạn chế xét whitelist, chứng chỉ theo loại người và cấp an toàn |
| Ngày hẹn của khách | Person/Visitor.checkInDenialReason | Đa hình: nhân sự vào ngày nào cũng được, khách chỉ đúng ngày hẹn |
| Parse và ghi CSV | CsvHelper | Dùng lại bởi bốn repository; phải có một bộ codec dùng chung |
| Lưu/nạp | Facade điều phối Service/Repository; CsvHelper ghi đè từng file | Không cần lớp gom dữ liệu, lịch sử phiên bản hay khóa phiên cho ứng dụng console một người dùng |
| Nhập liệu, màu, thông báo | InputHelper, ConsoleTheme, controller/view | Service không còn in ra console |

Tách thêm `AttendancePolicy`/`IncidentPolicy` khi một quy tắc có nhiều biến
thể (ca đêm, chuyển khu vực, phê duyệt nhiều bước), được dùng ở nhiều nơi,
hoặc cần kiểm thử độc lập với repository. Hiện tách thêm chỉ làm tăng số
file mà chưa giảm đáng kể độ phức tạp. Không cần tự viết tìm kiếm/sắp xếp
thay cho Map, List, Set và thư viện chuẩn.

## CSV nên tự viết hay dùng thư viện

- **Bản nộp WS6:** giữ CsvHelper có kiểm thử. Kế hoạch nội bộ WS5 ghi định
  hướng Java Core và không dùng thư viện ngoài; project hiện không có Maven
  hoặc Gradle. Đây là ràng buộc trong hồ sơ nội bộ, chưa xác minh độc lập
  được là lệnh cấm chính thức của giảng viên.
- **Bản tiếp tục sau môn học:** ưu tiên Apache Commons CSV khi được phép
  thêm dependency. Thư viện đọc/ghi nhiều biến thể CSV, giảm gánh nặng duy
  trì codec. Khóa phiên bản release đã kiểm thử, không dùng phiên bản
  SNAPSHOT chỉ vì trang API mặc định đang hiển thị nó.
- **Nhiều người dùng hoặc dữ liệu lớn:** chuyển persistence sang cơ sở dữ
  liệu có transaction; không phát triển thêm khóa lồng nhau hoặc thread ghi
  bốn file CSV riêng lẻ. CSV tiếp tục là định dạng import/export.

Apache công bố Commons CSV hỗ trợ đọc/ghi các biến thể CSV và Java 8 trở
lên: [tài liệu chính thức](https://commons.apache.org/proper/commons-csv/).
Lựa chọn dùng thư viện ở giai đoạn sau là đánh giá thiết kế cho project này,
không phải kết luận rằng thư viện tự giải quyết giao dịch nhiều file hay đồng bộ dữ liệu.

## Giới hạn có chủ đích

Hệ thống được thiết kế để chạy 5-10 năm với giới hạn rõ ràng, thay vì "càng
lớn càng tốt":

- **Mã định danh:** phần số tối đa 9 chữ số (999.999.999), lưu bằng `int`.
  2.000 người quẹt 4 lần/ngày trong 10 năm tạo khoảng 29 triệu bản ghi điểm
  danh, nhỏ hơn giới hạn hơn 30 lần. Trước đây dùng `BigInteger` để không bị
  tràn số với mã dài tùy ý; giới hạn cụ thể dễ giải thích và kiểm thử hơn.
  Khi chạm giới hạn, hệ thống báo lỗi rõ ràng thay vì treo hay tràn số.
- **Mã nội bộ nhân sự:** chọn ngẫu nhiên trong P10000-P99999 (90.000 người)
  để không lộ thứ tự đăng ký; hết dải thì báo lỗi thay vì lặp vô hạn.
- **Ghi file:** ghi đè trực tiếp bằng `Files.write`. Đã gỡ ghi qua file tạm +
  `ATOMIC_MOVE`, `FileChannel`, khóa thư mục `DataDirectoryLock` và `synchronized`
  vì ứng dụng là console một người dùng, một luồng. Đổi lại: nếu chương trình
  dừng giữa lúc ghi, file có thể không đầy đủ và lần khởi động sau sẽ từ chối
  file đó; không nên mở hai phiên trên cùng thư mục dữ liệu.
- **Mật khẩu:** lưu dạng chữ thường trong `accounts.csv` (không đưa lên Git,
  không xuất ra báo cáo) sau khi gỡ PBKDF2. Đây là giới hạn đã biết; thư mục
  dữ liệu phải được giữ riêng cho quản trị viên.

## Thứ tự nhiệm vụ tiếp theo

1. **Đồng bộ hồ sơ nộp bài:** cập nhật Workshop 6 và Final Report, hai AI Audit
   Log theo code hiện tại (267 ca kiểm thử, bỏ menu CSV validation, cấp an toàn, ngày hẹn, gỡ PBKDF2,
   atomic move, activity log, thông báo và migration). Render lại DOCX/PDF.
2. **Slide thuyết trình Final** sau khi hồ sơ đã khớp code.
3. **Nếu mở rộng triển khai:** SQLite cho một máy hoặc DB server cho nhiều
   máy; lúc đó mới cần transaction và khóa, CSV chỉ còn là định dạng import/export.
