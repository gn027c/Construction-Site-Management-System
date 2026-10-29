# Workshop 6 triển khai và nghiệm thu

Bản hiện tại dùng luồng **Controller → Service → Repository → CSV**.
Theo yêu cầu đơn giản hóa, đã bỏ package `src/state`, AppState, StateStore,
snapshot, manifest và CURRENT khỏi luồng chạy. Bốn CSV là nguồn dữ liệu
được đọc và ghi trực tiếp.

## Các phần đã hoàn thành

- CSV hỗ trợ dấu phẩy, dấu nháy kép, ô nhiều dòng, UTF-8 BOM và ghi qua file tạm.
- Thời gian điểm danh chỉ nhận `yyyy-MM-dd HH:mm:ss`; không chuyển đổi định
  dạng cũ. DateTimeHelper dùng chung khi tạo bản ghi và kiểm tra lúc đọc/ghi.
- Worker lưu đủ chứng chỉ, nghề, tổ đội; Visitor lưu đủ ngày đến.
  Thông tin nghề/tổ đội chưa biết được ghi rõ `Unknown` trong CSV; ngày đến
  chưa biết là ô trống. Loader không tự tạo ngày giả hoặc bù cột thiếu.
- Ngày đến Visitor dùng `dd/MM/yyyy`, kiểm tra bằng DateTimeHelper cả ở
  console và model. Timestamp AttendanceRecord cũng được kiểm tra khi tạo/sửa.
- Service kiểm tra chu kỳ CHECK_IN → CHECK_OUT và khu vực khi ra.
- Sự cố theo OPEN → ASSIGNED → RESOLVED; người được phân công phải tồn tại.
- Menu cấp/thu hồi quyền khu vực; whitelist kết hợp chứng chỉ hợp lệ.
- Console có màu và chế độ tắt màu, xử lý EOF và lỗi đọc/ghi rõ ràng.
- Xuất bốn CSV ra thư mục riêng, giảm nguy cơ ô bị diễn giải thành công thức.

## Đọc và ghi dữ liệu

`SiteManagerController` tạo bốn Service và các controller nhánh. Main gọi
`loadAllData()` khi khởi động. Facade nạp bốn CSV vào các Service mới, kiểm
tra tham chiếu rồi mới dùng các Service đó. Nếu một file lỗi, dữ liệu đang
làm việc vẫn được giữ.

Sau thao tác thêm/sửa/cấp quyền/điểm danh/xử lý sự cố, controller gọi lưu.
Xem danh sách không tự ghi file. Menu dữ liệu cho phép lưu thủ công, nạp
lại sau xác nhận, hoặc xuất CSV. Khi thoát bình thường hoặc hết input,
chương trình lưu dữ liệu rồi đóng.

Mã điểm danh tiếp theo được tính lại từ số REC lớn nhất trong attendance.csv
sau khi nạp thành công. Không có file riêng lưu recordCounter.

`DataDirectoryLock` giữ khóa không chờ trên `.smartsite.lock` trong phiên
console để từ chối phiên SmartSite thứ hai dùng cùng thư mục dữ liệu.
Lớp này chỉ giữ khóa file, không lưu dữ liệu nghiệp vụ hay phiên bản dữ liệu.

## Giới hạn cần biết

- Thay thế từng CSV là atomic move; bốn CSV không phải một transaction chung.
  Nếu một file ghi lỗi, chương trình nêu tên file lỗi và cho biết các file
  khác có thể đã ghi. Sửa nguyên nhân rồi thử Save lại.
- Không có lịch sử phiên bản, checksum hay khôi phục snapshot. Không cam kết
  giữ thao tác nhập dở hoặc dữ liệu chưa lưu khi mất điện/kill tiến trình.
- Khóa chỉ bảo vệ các phiên SmartSite tuân thủ khóa, không chặn ứng dụng khác
  tự sửa CSV. Service/Repository không được thiết kế cho nhiều thread ghi đồng thời.
- `requiredSafetyLevel` mô tả mức nguy hiểm; chưa so sánh cấp độ chứng chỉ
  của từng người vì model chưa có thuộc tính này. Quy tắc hiện tại là
  whitelist + chứng chỉ Boolean. Đăng nhập/RBAC menu chưa được triển khai.
- Mã nhân sự `NONE` có thể trùng ký hiệu whitelist rỗng; mã chứa `;` gây
  nhập nhằng khi lưu whitelist. Đây là hai ca còn cần sửa validation/codec.
- CSV mẫu có check-in lặp ATT-006 và check-out không khớp chu kỳ ATT-008.
  Không tự sửa lịch sử hoặc đoán dữ liệu nghề/tổ đội/ngày đến đã thiếu.

## Cách chạy

Từ gốc project trong PowerShell với JDK 8 trở lên:

```powershell
$sources = @(Get-ChildItem src -Recurse -Filter '*.java' | ForEach-Object FullName)
javac -encoding UTF-8 -d bin $sources
java '-Dfile.encoding=UTF-8' -cp bin Main

# Toàn bộ kiểm thử
./scripts/test.ps1
```

- Đổi thư mục CSV: `java '-Dsmartsite.dataDir=D:/SmartSiteData' -cp bin Main`.
  Thư mục mới bắt đầu trống; bộ dữ liệu đã có phải đủ bốn CSV.
- Bật/tắt màu bằng `-Dsmartsite.color=always` hoặc `-Dsmartsite.color=never`.
  Biến NO_COLOR ưu tiên tắt; output redirect mặc định không có ANSI.
- Menu **5 → 1**: xác nhận rồi nạp lại CSV; **5 → 2**: lưu CSV.
- Menu **5 → 3**: chọn thư mục cha để xuất persons/zones/attendance/incidents
  và README. Mỗi lần xuất tạo thư mục riêng, không ghi đè lần trước.
- Export dùng dữ liệu đang có trong RAM. Ô bắt đầu bằng `=`, `+`, `-`, `@`
  sau trim được thêm apostrophe để xem trong spreadsheet. CSV nghiệp vụ giữ
  giá trị gốc; export phục vụ báo cáo, không phải bản backup để import lại.

## Kết quả kiểm thử sau khi gỡ state

Ngày 29/10/2026, Java 8: **96/96 ca qua** sau khi rà soát toàn bộ ngày giờ.

| Bộ kiểm thử | Số ca |
| --- | ---: |
| HistoricalMatrixTest — đúng 25 kịch bản WS6 gốc | 25 |
| AttendanceIncidentTest | 14 |
| AttendanceTimestampTest — định dạng duy nhất, ngày/giờ hợp lệ và từ chối CSV cũ | 8 |
| DateTimeHelperTest — ngày nhuận, định dạng ngày, nhập lại và biên năm | 4 |
| VisitorDateTest — ngày sai, ngày chưa biết, schema CSV và export | 8 |
| PersonnelTest | 9 |
| ZoneTest | 7 |
| CsvHelperTest | 4 |
| CsvPersistenceTest — nạp/lưu trực tiếp, nạp lỗi, export, khóa và xem không ghi | 10 |
| ConsoleTest — EOF, menu, ngày hợp lệ, màu và kill sau khi đã lưu CSV | 7 |

Test đọc fixture gốc nhưng mọi file ghi đều nằm trong thư mục tạm riêng.
Log lần chạy mới: `output/date-time-audit-tests.txt`. Thư mục build test:
`bin/csv-tests`. Các kết quả snapshot của phiên bản trước không được dùng
làm bằng chứng cho phiên bản hiện tại.

## Nhánh và commit

Các sửa lỗi trước đây đã được chia theo module trên các nhánh Khang, Tuấn,
Thiên, Lợi và tích hợp tại `codex/ws6-integration`.

Thay đổi gỡ state nằm trên **codex/remove-state-layer**, commit **8408ec6**:
`refactor(csv): remove state snapshots and restore direct repository persistence`.
Commit giữ nguyên các sửa lỗi CSV, màu console, xuất file và kiểm soát nghiệp vụ.
Chưa push hoặc merge vào main.

Nhánh `loint-attendance-timestamp-format` tiếp tục từ bản đã gỡ state:
thống nhất thời gian điểm danh qua DateTimeHelper và loại bỏ chuyển đổi
CSV thời gian cũ. 15 timestamp trong fixture đã đúng định dạng nên giữ
nguyên giá trị; 8 ca kiểm thử mới bảo vệ hợp đồng định dạng này.

Nhánh `codex/date-time-validation` tiếp tục rà soát ngày giờ: dùng chung
formatter ngày ở InputHelper và Visitor, chặn sửa timestamp sai tại model,
và chuẩn hóa persons.csv thành đúng chín cột cho mọi loại nhân sự.
Chi tiết vị trí và lỗi được ghi trong [DATE_TIME_REVIEW.md](DATE_TIME_REVIEW.md).

## Phần còn lại của workflow

Đã khảo sát thuật toán/thư viện và đối soát Report/Audit Log. Chưa chỉnh trực
tiếp DOCX/XLSX. Final Report còn mẫu BookManager/AddBook; Audit Log còn hướng
dẫn split CSV sai và kết quả cũ. Cần đồng bộ hồ sơ với phiên bản code mới,
cập nhật minh chứng thật và render/kiểm tra lại trước khi nộp.
