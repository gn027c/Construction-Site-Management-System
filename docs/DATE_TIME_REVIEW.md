# Rà soát ngày giờ trong SmartSite

Ngày kiểm tra 29/10/2026. Phạm vi gồm toàn bộ src, các fixture CSV và test.
Kết quả: format được khai báo một lần cho mỗi kiểu dữ liệu trong
DateTimeHelper; không tự đổi định dạng cũ và không đoán ngày bị thiếu.

Sau lượt chuẩn hóa model, ModelValidation gọi DateTimeHelper và đổi lỗi
parse thành DataValidationException có tên field. Bộ kiểm thử tổng thể
hiện là 116 ca; phần kiểm thử ngày giờ được giữ, các kiểm tra helper riêng
vẫn sử dụng DateTimeParseException cho lỗi codec ngày giờ.

## Các vị trí sử dụng

| Vị trí | Dữ liệu/thao tác | Hợp đồng hiện tại |
| --- | --- | --- |
| AttendanceService.processAttendance | Lấy LocalDateTime.now rồi tạo timestamp | DateTimeHelper.formatTimestamp, yyyy-MM-dd HH:mm:ss |
| AttendanceRecord constructor/setTimestamp | Tạo và sửa thời điểm quẹt thẻ | DateTimeHelper.parseTimestamp; từ chối sai trước khi thay giá trị |
| AttendanceRepository.save/loadFromCsv/saveToCsv | Nhận và đọc/ghi bản ghi CSV | Kiểm tra timestamp bằng cùng helper; nạp lỗi giữ danh sách cũ |
| InputHelper.validateDate/getDate | Kiểm tra và nhập ngày từ console | DateTimeHelper.parseDate; trim input của người dùng, nhập lại khi sai |
| PersonnelController.addPersonFlow | Nhập ngày đến Visitor | Prompt lấy nhãn DATE_FORMAT từ helper; ngày bắt buộc khi thêm qua console |
| Visitor constructor/setEntryDate | Tạo và sửa ngày đến | dd/MM/yyyy, ngày thực; ô trống biểu thị ngày chưa biết; null bị từ chối |
| Visitor.displayDetails/toCsvLine | Hiển thị và ghi ngày đến | Hiển thị “Chưa có thông tin” cho ngày trống, CSV giữ ô trống |
| PersonRepository.loadFromCsv/saveToCsv | Lưu/nạp nhân sự và ngày đến | Chính xác header và chín cột; Visitor được kiểm tra tại constructor |
| SiteManagerController.exportCsv, CsvExportService | Xuất ngày giờ trong báo cáo | Dùng giá trị chuẩn từ model; không thêm formatter hoặc chuyển đổi ngày khác |
| data/attendance.csv | 15 timestamp hiện có | Tất cả đã đúng yyyy-MM-dd HH:mm:ss, giữ nguyên giá trị |
| data/persons.csv | Ngày đến và schema nhân sự | Đã chuẩn hóa chín cột; V01/V02 vẫn chưa có ngày, để trống |

Incident hiện không có trường ngày báo cáo/ngày giải quyết. Đây là trường
dữ liệu chưa có, không phải vị trí formatter bị bỏ sót. Các lệnh
System.nanoTime, TimeUnit và FileTime trong test phục vụ timeout hoặc phát
hiện ghi file; không dùng để biểu diễn ngày giờ nghiệp vụ.

## Những lỗi đã sửa

1. InputHelper tự tạo formatter mỗi lần kiểm tra ngày, chưa dùng chung với
   các đường khác. Đã gom vào hai DateTimeFormatter bất biến, cache tĩnh
   trong DateTimeHelper, dùng Locale.ROOT và ResolverStyle.STRICT.
2. Visitor chỉ kiểm tra chuỗi không rỗng nên constructor/setter nhận được
   ngày như 31/02/2026. Đã kiểm tra ngày thực ở model; setter lỗi giữ giá trị cũ.
3. AttendanceRecord nhận timestamp tùy ý dù repository đã kiểm tra. Đã
   kiểm tra ngay constructor/setter để chặn model không hợp lệ sớm hơn.
4. PersonRepository từng bù ngày bằng chuỗi Unknown (legacy) khi thiếu cột.
   Đã bỏ nhánh tương thích schema cũ, chỉ nhận schema chín cột đầy đủ.
5. persons.csv thiếu cột ngày đến cho Visitor và thiếu nghề/tổ đội Worker.
   Đã chuẩn hóa dữ liệu có thật, dùng Unknown cho nghề/tổ đội chưa biết và
   ô trống cho ngày chưa biết. Không tự suy ra ngày đến từ lịch sử điểm danh.
6. Import java.text.ParseException/SimpleDateFormat đã lỗi thời, không còn
   dùng trong InputHelper; đã loại bỏ.

## Hai kiểu dữ liệu được giữ riêng

- **Thời điểm điểm danh:** yyyy-MM-dd HH:mm:ss, ví dụ 2026-10-29 07:05:09.
  Không nhận dấu T, timezone, phần mili giây, thiếu giây hoặc ngày kiểu cũ.
- **Ngày đến:** dd/MM/yyyy, ví dụ 29/10/2026. Không tự thêm giờ 00:00:00.
  CSV có thể để trống nếu chưa có thông tin; chuỗi tùy ý và khoảng trắng
  trong ô ngày không được coi là ngày chưa biết.
- Phạm vi năm được kiểm tra thống nhất là 0001–9999. Các pattern Java dùng
  uuuu để strict parsing; nhãn hiển thị cho người dùng vẫn là yyyy.
- LocalDateTime.now dùng giờ địa phương của JVM; CSV không có timezone.
  Chưa thêm chuyển múi giờ hoặc đồng bộ giữa nhiều máy. Đây là giới hạn
  được ghi nhận cho triển khai nhiều địa điểm, không tự đổi mốc giờ dữ liệu cũ.

## Kiểm thử và commit

- **96/96 ca qua**, gồm 25 tình huống WS6 gốc, 8 kiểm thử timestamp,
  4 kiểm thử helper và 8 kiểm thử ngày Visitor cùng các bộ hồi quy khác.
- Kiểm tra năm nhuận, ngày không tồn tại, định dạng khác, thiếu phần giờ,
  ký tự dư, null, năm không hỗ trợ, setter lỗi giữ giá trị cũ, CSV lỗi giữ
  dữ liệu đã nạp và ngày được giữ nguyên qua console → CSV → export.
- Log thực thi: output/date-time-audit-tests.txt.
- Nhánh: codex/date-time-validation.
- bde9e3a: dùng chung date parser và kiểm tra timestamp tại model.
- 35f91cc: kiểm tra ngày Visitor và chuẩn hóa schema CSV nhân sự.
