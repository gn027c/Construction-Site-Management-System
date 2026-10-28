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

Các nhánh chức năng lấy phần CSV dùng chung làm nền. Mỗi sửa lỗi/tính năng
là một commit có scope cụ thể; nhánh tích hợp giữ nguyên các commit đó.
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

## Các lỗi cần sửa

1. CSV `split(",")` phá ô có dấu phẩy/nháy; reader theo dòng vật lý phá ô nhiều dòng.
2. Worker thiếu trade/team và Visitor thiếu entryDate trong dữ liệu lưu.
3. Loader bỏ qua dòng hỏng hoặc tự đổi enum lỗi thành giá trị hợp lệ, có thể
   mất dữ liệu/trạng thái mà vẫn báo thành công.
4. Facade bỏ qua kết quả đọc/ghi; Main báo thành công sai và lặp vô hạn khi EOF.
5. API cấp/thu hồi quyền chưa có menu; whitelist mới tạo không thể quản lý qua console.

## Giới hạn lưu trữ

Ghi mỗi CSV qua file tạm cùng thư mục rồi thay thế file đích. Đây là bảo vệ
từng file, không phải giao dịch nguyên tử cho cả bốn file. Không cam kết lưu
thay đổi trong RAM khi tiến trình bị kill hoặc mất điện.

## Kiểm thử

Kết quả 24/25 trước đây là lịch sử, không dùng thay cho lần chạy mới.
Các test hồi quy phải kiểm tra loại exception cụ thể và dữ liệu thực tế,
chạy bằng dữ liệu tạm, không ghi đè `data/` của dự án.
