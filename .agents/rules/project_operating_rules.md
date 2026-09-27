# 🛠️ WORKSPACE RULE: QUY CHUẨN HOẠT ĐỘNG DỰ ÁN SMARTSITE (PROJECT OPERATING RULES)
> Cập nhật: 27/09/2026 | Áp dụng: Toàn bộ quá trình phát triển, kiểm thử, viết báo cáo và lập AI Audit Log

---

## 1. QUY CHUẨN ĐẶT TÊN FILE (FILE NAMING CONVENTIONS)
- **AI Audit Log (Excel)**:
  - Bắt buộc theo định dạng: `AI_AuditLog_Workshop_<num>_Group2_SE211059.xlsx`
  - File tổng kết cuối kỳ: `AI_AuditLog_Final_Group2_SE211059.xlsx`
  - Tuyệt đối không dùng tên dạng `PRO192_...` hoặc thiếu mã nhóm `Group2`.
- **Project Report (Word)**:
  - Bắt buộc theo định dạng: `Workshop<num>_Group2_Topic7.docx`
  - File tổng kết cuối kỳ: `PRO192_Final_Report_Group2_SmartSite.docx`

---

## 2. KỸ THUẬT ĐỒNG BỘ OPENXML EXCEL CÓ CHỨA HÌNH ẢNH
- Khi đồng bộ các dòng có hình ảnh minh chứng giữa các file `.xlsx`:
  - Thư viện `openpyxl` tiêu chuẩn không tự động sao chép các đối tượng Drawing và Media.
  - Phải duy trì toàn vẹn 4 thành phần OpenXML:
    1. Media binary: `xl/media/image*.png`.
    2. Drawing XML: `xl/drawings/drawing1.xml` và quan hệ `xl/drawings/_rels/drawing1.xml.rels`.
    3. Sheet Relationships: Thẻ `<Relationship Id="rId3" ... Target="../drawings/drawing1.xml"/>` trong `sheet*.xml.rels` và kích hoạt `<drawing r:id="rId3"/>` trong `sheet*.xml`.
    4. Content Types: Khai báo extension `.png` và Override part `/xl/drawings/drawing1.xml` trong `[Content_Types].xml`.
  - **Đồng bộ kích thước hiển thị**:
    - Phải đồng bộ `row_dimensions[row].height` (hàng chứa ảnh cần đặt từ 250pt - 280pt).
    - Cột chứa ảnh (`Evidence`) phải mở rộng `column_dimensions['H'].width = 96.0` để ảnh không bị tràn viền hoặc che khuất dữ liệu.

---

## 3. TIÊU CHUẨN KHỬ AI SLOP TRONG AI AUDIT LOG (RBL FRAMEWORK)
- Khi viết Human Delta trong Audit Log, bắt buộc tuân thủ 4 câu hỏi:
  1. **Critical Thinking**: Phân tích đúng bản chất kỹ thuật, truy tìm nguyên nhân gốc rễ (Root Cause: Iterator ngầm, modCount vs expectedModCount, độ phức tạp O(N log N) lãng phí của Stream/Sort).
  2. **Contextualization**: Đối chiếu với bối cảnh thực tế của đề bài: Ứng dụng Java Console đơn luồng (không được để Unchecked Exception làm sập tiến trình main gây mất dữ liệu RAM), nghiệp vụ công trường (quét thẻ theo ca, xử lý hàng loạt sự cố).
  3. **Creative Synthesis**: Đưa ra giải pháp tối ưu, sạch sẽ (Reverse loop O(k), Java 8 `removeIf()`, kiểm soát State Transition).
  4. **Decision Ownership**: Quyết định dứt khoát của sinh viên kèm căn cứ kỹ thuật.
- **Loại bỏ hoàn toàn văn phong AI**: Cấm cấu trúc "không phải X mà là Y", cấm từ ngữ thổi phồng, câu mở/kết sáo rỗng.

---

## 4. KỶ LUẬT CHẠY THỬ NGHIỆM VÀ KIỂM CHỨNG TẠI SCRATCH/
- Toàn bộ file kiểm thử thuật toán, tái hiện lỗi ngoại lệ (reproduce bug) được lưu trữ tại `scratch/`.
- Khi hướng dẫn người dùng hoặc viết script kiểm thử:
  - Nếu terminal đang đứng tại `scratch/`, chạy trực tiếp `javac <File>.java; java <File>`.
  - Luôn kiểm tra tính tương thích bảng mã UTF-8 trên Windows PowerShell.
