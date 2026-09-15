# Đề tài 7: SmartSite: Hệ Thống Quản Lý Công Trường Tích Hợp AI

## 1. Bối cảnh
Ứng dụng này hỗ trợ quản lý công việc hàng ngày của công nhân, nhà thầu và khách tham quan tại một công trường xây dựng, xoay quanh ba vai trò phối hợp:
- **Công nhân, nhà thầu và khách tham quan**: Điểm danh vào/ra bằng mã đơn giản thay vì quét sinh trắc học, có thể xem lại lịch sử điểm danh của chính mình.
- **Cán bộ An toàn (Safety Officer)**: Ghi nhận sự cố an toàn khi phát sinh, phân công xử lý và theo dõi cho đến khi giải quyết xong; hoàn toàn dựa trên báo cáo thủ công thay vì phát hiện tự động qua video.
- **Chỉ huy trưởng / Quản lý công trường (Site Manager)**: Quản lý danh sách nhân sự được phép vào công trường, thiết lập các khu vực thi công chung và khu vực hạn chế, đồng thời xem các báo cáo điểm danh và sự cố.

Do dự án không tích hợp camera hay mô hình AI để phân tích, tất cả ghi nhận an toàn đều do nhân viên nhập thủ công thay vì nhận diện tự động. Hệ thống yêu cầu phân quyền dựa trên vai trò (RBAC) cơ bản để công nhân, cán bộ an toàn và quản lý chỉ thấy đúng chức năng thuộc quyền hạn của mình. Toàn bộ dữ liệu điểm danh và sự cố được lưu trữ trong cơ sở dữ liệu quan hệ / tập tin, hỗ trợ xuất Excel đơn giản để làm báo cáo. Tổng thể các yêu cầu này tạo nên một quy trình quản lý nhân sự công trường tinh gọn.

---

## 2. Yêu cầu Chức năng

### Vai trò: Công nhân / Nhà thầu / Khách tham quan
Người dùng có thể:
- Điểm danh vào / ra (nhập mã).
- Xem lịch sử điểm danh của chính mình.

### Vai trò: Cán bộ An toàn (Safety Officer)
Cán bộ an toàn có thể:
- Ghi nhận sự cố an toàn (thủ công - không nhận diện PPE bằng AI).
- Phân công xử lý sự cố và cập nhật trạng thái sự cố.

### Vai trò: Chỉ huy trưởng (Site Manager)
Chỉ huy trưởng có thể:
- Quản lý danh sách công nhân / nhà thầu / khách tham quan.
- Quản lý các khu vực thi công và khu vực hạn chế.
- Xem báo cáo điểm danh và sự cố theo tuần / tháng.

---

## 3. Yêu cầu Phi chức năng
- Không yêu cầu nhận diện khuôn mặt hay xử lý video bằng AI - điểm danh thông qua mã nhập thủ công.
- Phân quyền theo vai trò (RBAC) cơ bản.
- Dữ liệu điểm danh và sự cố được lưu trữ trong file văn bản / nhị phân, hỗ trợ xuất dữ liệu đơn giản.

---

## 4. Công nghệ Đề xuất & Thiết kế Hướng đối tượng (OOP)
- **Ngôn ngữ**: Java
- **Kiến trúc OOP**:
  - `Person` là lớp cha kế thừa bởi `Worker`, `Contractor`, `Visitor`.
  - `Zone` là lớp cha kế thừa bởi `RestrictedZone`.
- **Giao diện**: Ứng dụng Console đơn giản.
- **Lưu trữ dữ liệu**: Lưu trữ file cho các đối tượng `Person`, `AttendanceRecord`, `Incident` và `Zone`.
