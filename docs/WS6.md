# Workshop 6: phạm vi triển khai và nghiệm thu

WS6 yêu cầu hoàn chỉnh chương trình và chạy thực tế toàn bộ ma trận kiểm thử;
hạn nộp 08/11/2026. Tài liệu gốc và báo cáo Word được quản lý riêng trong các
thư mục nội bộ. File này ghi nhận phạm vi code, cách kiểm chứng và giới hạn.

## Công việc và nhánh

| Nhánh | Phạm vi | Tiêu chí hoàn thành |
| --- | --- | --- |
| `khanghnh-final-integration` | WS6 1.1–1.5: CSV dùng chung, lưu/nạp, console | Đọc đúng ô có dấu phẩy, nháy, xuống dòng; báo lỗi I/O; kết thúc khi hết input |
| `tuantna-final-verification` | WS6 2.1–2.4: nhân sự | Lưu/nạp đủ Worker, Contractor, Visitor; giữ tương thích CSV cũ; kiểm tra dữ liệu |
| `thientt-final-verification` | WS6 3.1–3.4: khu vực | Cấp/thu hồi quyền từ menu; giữ whitelist khi lưu/nạp; chặn thiếu chứng chỉ |
| `loint-final-verification` | WS6 4.1–4.4: điểm danh, sự cố | CSV sự cố không mất dữ liệu; chặn sai chu kỳ; giữ trạng thái khi nạp lỗi |
| `codex/ws6-integration` | Tích hợp, kiểm thử và hướng dẫn | Biên dịch Java 8; kiểm thử tự động có kết quả và mã thoát rõ ràng |

Các nhánh chức năng được xếp nối tiếp theo phụ thuộc CSV → nhân sự → khu vực
→ điểm danh/sự cố → state/view. Mỗi sửa lỗi/tính năng là một commit có scope
cụ thể; nhánh tích hợp giữ nguyên các commit đó. Nhánh Khang được fast-forward
qua các module đã hoàn tất trước khi thêm state. Không đổi tác giả Git thành
tên thành viên; tên nhánh thể hiện phạm vi phân công.
Nền triển khai là `7798417`, bao gồm hai sửa lỗi có sẵn về mã REC và timestamp.

## Quy tắc nghiệp vụ được giữ rõ ràng

- Điểm danh: CHECK_IN → CHECK_OUT; không vào hai khu vực cùng lúc, không ra
  trước khi vào hoặc ra sai khu vực.
- Sự cố: OPEN → ASSIGNED → RESOLVED. Chỉ phân công cho nhân sự tồn tại.
  Các ghi chú cũ nói REPORTED/IN_PROGRESS/CLOSED không phản ánh code hiện tại.
- Khu hạn chế: phải có tên trong whitelist **và** chứng chỉ hợp lệ.
  Worker dùng mã kết thúc `_SAFE`; Contractor dùng hợp đồng bắt đầu `CTR-`;
  Visitor không có chứng chỉ. Đây là quy ước mô phỏng của đồ án.
- Theo lựa chọn được cho phép tại WS6 3.4, `requiredSafetyLevel` là metadata
  mô tả mức nguy hiểm. Chưa so sánh LOW/MEDIUM/HIGH/CRITICAL với từng người
  vì Person chưa có thuộc tính cấp độ chứng chỉ. Ghi rõ giới hạn thay vì tự
  suy diễn một chứng chỉ Boolean thành cấp độ.
- Enum Role hiện mô tả loại nhân sự; đăng nhập/RBAC menu, AI và báo cáo
  thống kê tuần/tháng không được nghiệm thu như chức năng đã triển khai.

## Các lỗi đã sửa

1. CSV `split(",")` phá ô có dấu phẩy/nháy; reader theo dòng vật lý phá ô nhiều dòng.
2. Worker thiếu trade/team và Visitor thiếu entryDate trong dữ liệu lưu.
3. Loader bỏ qua dòng hỏng hoặc tự đổi enum lỗi thành giá trị hợp lệ, có thể
   mất dữ liệu/trạng thái mà vẫn báo thành công.
4. Facade bỏ qua kết quả đọc/ghi; Main báo thành công sai và lặp vô hạn khi EOF.
5. API cấp/thu hồi quyền chưa có menu; whitelist mới tạo không thể quản lý qua console.

## Giới hạn lưu trữ

`StateStore` giữ một khóa file bằng `tryLock()` trong cả phiên chạy. Mỗi lần
checkpoint ghi đủ bốn CSV vào một thư mục UUID mới, nạp thử và kiểm tra tham
chiếu, ghi manifest SHA-256 rồi thay `CURRENT` bằng atomic move. Reader chỉ
nạp bộ snapshot đã được công bố. Không hỗ trợ atomic move thì lưu thất bại
rõ ràng; không dùng fallback có thể ghi dở file.

Ứng dụng checkpoint sau mỗi thao tác trong submenu và khi thoát/EOF. Chỉ
đổi toàn bộ AppState sau khi nạp thành công cả bốn module. CSV gốc trong
`data/` là dữ liệu khởi tạo, không bị ghi đè sau khi chuyển sang snapshot.

Snapshot chỉ cam kết khôi phục dữ liệu đã checkpoint. Không cam kết giữ
thao tác nhập dở, chống mọi lỗi phần cứng/mất điện, hay ngăn ứng dụng khác
phớt lờ khóa và sửa file. Checksum phát hiện hỏng dữ liệu, không phải chữ ký
xác thực chống tấn công. Console có một luồng sở hữu AppState; không công bố
Service/Repository như API an toàn cho nhiều thread.

Snapshot được giữ lại, chưa có cơ chế tự dọn hoặc menu chọn bản phục hồi cũ.
Lần lưu lỗi có thể để lại thư mục UUID chưa công bố; reader bỏ qua thư mục đó.
Không xóa hay tự sửa dữ liệu lịch sử bất thường trong bộ mẫu: ATT-006 là
check-in lặp của W01 và ATT-008 là check-out sau một check-out khác. Thuật
toán cho thao tác mới dựa trên bản ghi cuối cùng theo thứ tự CSV, không sắp
xếp lại lịch sử. Cần đối soát các bản ghi này với chủ dữ liệu trước khi sửa.

## Kiểm thử

Lần nghiệm thu ngày 29/10/2026: **76/76 ca qua** trên Java 8.

| Bộ kiểm thử | Số ca qua |
| --- | ---: |
| HistoricalMatrixTest — 25 kịch bản gốc, kiểm tra đúng loại exception | 25 |
| AttendanceIncidentTest — FSM, khởi động lại, CSV sự cố | 14 |
| PersonnelTest — validation, CSV đầy đủ và tương thích dữ liệu cũ | 9 |
| ZoneTest — quyền, whitelist, nạp lỗi | 7 |
| CsvHelperTest — dấu phẩy, nháy, nhiều dòng, BOM và ghi lỗi | 4 |
| StateStoreTest — snapshot, checksum, tham chiếu, khóa và export | 10 |
| ConsoleTest — EOF, menu, màu, ngày hợp lệ và kill sau checkpoint | 7 |

`HistoricalMatrixTest` giữ các tình huống của ma trận gốc. Các test hồi quy
cùng mã TC ở các bộ còn lại là kiểm chứng bổ sung, không phải bản thay thế
ngầm cho định nghĩa tình huống cũ. Test đọc fixture gốc nhưng mọi file ghi
được đặt trong thư mục tạm riêng. Log lần chạy: `output/ws6-tests.txt`.

## Cách chạy

Từ gốc project trong PowerShell, với JDK 8 trở lên:

```powershell
$sources = @(Get-ChildItem src -Recurse -Filter '*.java' | ForEach-Object FullName)
javac -encoding UTF-8 -d bin $sources
java '-Dfile.encoding=UTF-8' -cp bin Main

# Chạy toàn bộ bộ kiểm thử
./scripts/test.ps1
```

- Đổi thư mục dữ liệu: `java '-Dsmartsite.dataDir=D:/SmartSiteData' -cp bin Main`.
  Thư mục hoàn toàn mới bắt đầu trống; bộ CSV cũ cần có đủ bốn file.
- Màu: `-Dsmartsite.color=always` để bật, `-Dsmartsite.color=never` để tắt.
  `NO_COLOR` ưu tiên tắt. Khi redirect output, mặc định không có ANSI.
- Menu **5 → 2**: lưu checkpoint. **5 → 1**: xác nhận rồi nạp state đã commit.
- Menu **5 → 3**: chọn thư mục cha để xuất bốn CSV và README. Mỗi lần xuất
  tạo thư mục mới, không ghi đè lần trước. Export dùng dữ liệu hiện có trong
  RAM, có thể khác checkpoint nếu lần lưu trước lỗi.
- CSV xuất có UTF-8 BOM; ô bắt đầu bằng `=`, `+`, `-`, `@` sau trim được thêm
  apostrophe để giảm nguy cơ diễn giải công thức. Snapshot nội bộ giữ giá
  trị gốc. Export phục vụ xem báo cáo, không phải backup dùng để import lại.
- Khi lưu lỗi, màn hình báo lỗi; thay đổi vẫn ở RAM. Khắc phục quyền/dung
  lượng/đường dẫn và thử Save lại. Không xóa CURRENT để xử lý lỗi.

## Lịch sử triển khai

| Commit | Nhánh lúc tạo commit | Nội dung |
| --- | --- | --- |
| `e10cff7` | `codex/ws6-plan` | Chốt phạm vi và tiêu chí WS6 |
| `22ae8cc` | `khanghnh-final-integration` | Bộ đọc CSV và ghi file nguyên tử |
| `57177f6` | `tuantna-final-verification` | Lưu đủ hồ sơ nhân sự |
| `9e0514f` | `thientt-final-verification` | Menu quyền và CSV khu vực |
| `24cfe45` | `loint-final-verification` | FSM và CSV sự cố |
| `4278ba0` | `khanghnh-final-integration` | Snapshot, khóa phiên và xuất CSV |
| `d043a20` | `codex/ws6-console-theme` | Màu prompt và thông báo |
| `52333c9` | `codex/ws6-integration` | Tách hiển thị khỏi Service |
| `28fe9ca` | `codex/ws6-integration` | Nghiệm thu ma trận gốc và phục hồi sau kill |

Tất cả thay đổi đã tích hợp tại `codex/ws6-integration`. Commit được tạo
local, chưa push hoặc merge vào `main`.
