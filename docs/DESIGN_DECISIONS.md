# Thuật toán và thư viện trong SmartSite

Quyết định cho WS6: giữ thuật toán nghiệp vụ trong Service, tách phần lưu
file vào Repository/CsvHelper, dùng Java SE 8 cho CSV và I/O. Không thêm một
package `algorithm` chỉ để chuyển nguyên phương thức sang file khác.

## Chọn cách tổ chức theo trách nhiệm

| Thành phần | Vị trí hiện tại | Lý do |
| --- | --- | --- |
| Chu kỳ vào/ra | AttendanceService | Cần phối hợp nhân sự, khu vực và bản ghi cuối cùng; quy tắc ngắn, đặc thù SmartSite |
| OPEN → ASSIGNED → RESOLVED | IncidentService | Một vòng đời dùng tại một module, chưa cần thư viện FSM |
| Kiểm tra quyền | Zone.checkAccess và RestrictedZone.checkAccess | Đa hình phù hợp mục tiêu OOP và mô hình đã có |
| Parse và ghi CSV | CsvHelper | Dùng lại bởi bốn repository; phải có một bộ codec dùng chung |
| Lưu/nạp và khóa phiên | Facade điều phối Service/Repository; DataDirectoryLock giữ khóa | Không cần lớp gom dữ liệu hoặc lịch sử phiên bản; khóa chỉ ngăn hai phiên console ghi cùng thư mục |
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

## Cơ sở cho thiết kế khóa và ghi file

`tryLock()` có thể trả `null` hoặc ném lỗi khi khóa chồng lấn; ứng dụng xử lý
hai trường hợp bằng lỗi mở phiên rõ ràng, không dùng vòng lặp chờ khóa.
Ứng dụng có một khóa phiên và một luồng sở hữu dữ liệu trong
console, không tạo chu trình chờ giữa nhiều khóa module. Xem
[FileChannel Java 8](https://docs.oracle.com/javase/8/docs/api/java/nio/channels/FileChannel.html).

Atomic move phụ thuộc filesystem và có thể không được hỗ trợ. SmartSite
trả lỗi lưu khi không thể thay file bằng atomic move; không tuyên bố mọi
filesystem/network share có độ bền như database. Xem
[Files Java 8](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html).

## Thứ tự nhiệm vụ tiếp theo

1. **Đồng bộ hồ sơ nộp bài:** bỏ mẫu BookManager/AddBook khỏi Final Report,
   sửa hướng dẫn split CSV trong Audit Log; cập nhật UML theo luồng
   Controller → Service → Repository → CSV, schema nhân sự và kết quả
   25/25 cùng 51 ca bổ sung. Render lại DOCX/PDF sau chỉnh sửa.
2. **Đối soát dữ liệu thật:** bổ sung thông tin `Unknown` và ngày đến còn trống, kiểm tra
   các lượt vào/ra cũ sai chu kỳ. Không đoán dữ liệu đã bị mất.
3. **Lịch sử cá nhân và báo cáo theo ngày/tuần:** lọc theo mã, khoảng thời
   gian và khu vực; xuất thống kê tổng số lượt và sự cố theo trạng thái.
4. **Quản lý nhân sự an toàn:** deactivation thay vì xóa hồ sơ đang có tham
   chiếu; tách loại nhân sự khỏi quyền tài khoản trước khi thêm đăng nhập.
5. **Import CSV:** preview lỗi từng dòng và xác nhận trước khi thay dữ liệu.
   Chỉ bổ sung backup/versioning khi có yêu cầu mới, không khôi phục cơ chế
   snapshot vừa được yêu cầu gỡ.
6. **Nếu mở rộng triển khai:** SQLite cho một máy hoặc DB server cho nhiều
   máy; lúc đó chuyển orchestration sang API/GUI thay vì chia sẻ CSV qua mạng.
