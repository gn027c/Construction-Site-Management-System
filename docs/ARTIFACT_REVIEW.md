# Đối soát Project Report và AI Audit Log

Kiểm tra ngày 29/10/2026, sau triển khai WS6. **Hồ sơ hiện có chưa sẵn sàng
nộp cùng phiên bản code mới.** Phần cần sửa gấp nhất là nội dung sách còn
trong Final Report, kết quả kiểm thử cũ, schema CSV cũ và hướng dẫn sửa CSV
sai trong Audit Log.

Đây là kết quả rà soát, không phải bản thay thế báo cáo. Các file DOCX,
PDF và XLSX gốc được đọc, không sửa hay tự bổ sung lời tự đánh giá cá nhân.
Các thư mục hồ sơ nội bộ vẫn nằm trong `.gitignore` theo quy tắc dự án.

## Phạm vi và bằng chứng

- Đọc cấu trúc và toàn bộ text trong paragraph/table của **8 DOCX**, gồm
  template, Final Report và WS1–WS6; kiểm kê **7 XLSX** gồm Final và WS1–WS6.
- Đối soát kỹ Final/WS6 với code hiện tại và test chạy thực tế; không xem
  các nhận xét/hình ảnh lịch sử ở WS1–WS5 là minh chứng cho code hôm nay.
- WS6 DOCX: 181 paragraph, 33 table, 10 hình inline. PDF đi kèm: 46 trang.
  Render PDF bằng Poppler, xem contact sheet cả 46 trang và phóng to phần
  kết quả tại trang 40–42. Đây không phải xác nhận mọi chữ trên mọi trang
  đều đã được xem ở kích thước 100%.
- Render bốn sheet WS6 bằng Artifact Tool; đối chiếu công thức với dữ liệu
  nguồn và cached values bằng openpyxl ở chế độ đọc, không ghi workbook.
- Công cụ `render_docx.py` thất bại do thiếu **LibreOffice soffice.exe**.
  Không thể xác nhận một lần render mới từ DOCX khớp PDF cũ. Cần render lại
  sau khi sửa nội dung; chưa chứng nhận DOCX sạch lỗi trình bày.
- `output/ws6-review/` giữ bản trích nội dung, inventory SHA-256 và hình
  preview để kiểm tra tại máy. `output/ws6-tests.txt` giữ log 76 ca kiểm thử.

## Project Report

| Mức | File và vị trí | Phát hiện | Việc cần làm |
| --- | --- | --- | --- |
| Cao | PRO192_Final_Report_Group2_SmartSite.docx, mục 3.1 và 4.1 | Còn `List<Book>`, pseudocode `AddBook`, code `BookManager`; mục 3.2 còn dấu chấm placeholder | Viết lại bằng luồng SmartSite; không nộp file này dưới tên Final khi nội dung còn mẫu sách |
| Cao | Workshop6_Group2_Topic7.docx, mục 6.1, 7.4 và Figure 7.1; PDF trang 40–41 | Kết quả 24/25 ngày 19/10 là lịch sử hợp lệ, nhưng không phản ánh phiên bản hiện tại | Giữ một dòng lịch sử; thêm kết quả mới **25/25 kịch bản gốc + 51/51 ca bổ sung**, tên nhánh/commit và log mới |
| Cao | WS6 mục 3.1, 5.1, 6.2 và sơ đồ UML | Luồng cũ dùng Facade.loadAllData/saveAllData và ghi đè bốn CSV gốc; thiếu AppState/StateStore | Cập nhật luồng snapshot, khóa phiên, manifest, CURRENT và callback checkpoint trong controller |
| Cao | WS6 mục 6.2, Table 6.2.1 và listing Worker/Visitor/PersonRepository | Schema hai extra và code mất trade/team/entryDate đã lỗi thời | Dùng schema ba extra; mô tả dữ liệu cũ thiếu thông tin là `Unknown (legacy)` |
| Cao | WS6 mục 7.4 và appendix incidents.csv | Các lỗi parser/nhân sự còn được ghi như chưa sửa; tripled quotes đã có trong fixture | Ghi commit sửa; giải thích parser mới giữ nội dung logic, không tự xóa nháy vốn có trong dữ liệu cũ |
| Vừa | WS6 Table 3.2, dòng purgeResolvedIncidents | Ghi purge vẫn duy trì permanent CSV audit trail | Không có kho audit append-only; purge loại khỏi state mới. Snapshot cũ chỉ còn vì chưa có retention, không hứa lưu vĩnh viễn |
| Vừa | WS6 mô tả ZoneRepository | Có chỗ dùng từ zone mapping, dễ hiểu là Map/O(1) | Code hiện là ArrayList và tìm tuyến tính O(n); chỉ tra nhân sự là LinkedHashMap/O(1) trung bình |
| Vừa | WS6 bảng Role và phần quyền | Enum có SAFETY_OFFICER/SITE_MANAGER nhưng không có xác thực/RBAC menu | Nêu rõ loại nhân sự và quyền tài khoản khác nhau; không nhận là chức năng đã cài |
| Vừa | WS6 mục 7.4, requiredSafetyLevel | Chưa dùng cấp độ trong checkAccess | Ghi rõ giới hạn được WS6 3.4 cho phép: whitelist + chứng chỉ Boolean, level là metadata |
| Vừa | WS6 appendix attendance.csv, ATT-006 và ATT-008 | Fixture có check-in lặp và check-out không khớp chu kỳ | Đối soát với chủ dữ liệu; không gọi tất cả 15 record là lịch sử FSM hợp lệ chỉ vì loader đọc đủ số dòng |
| Trình bày | PDF WS6 trang 3, 17, 19 và 42 | Trang 3 trống; trang 19 gần như chỉ có dấu đóng code, trang 42 chỉ còn hai dòng; nhiều trang listing có khoảng trắng lớn | Sắp lại page break/table/code block, tránh dòng code lẻ; render lại toàn bộ sau chỉnh sửa |

**Điểm đúng đã xác minh:** WS6 dùng vòng đời OPEN → ASSIGNED → RESOLVED;
25 kết quả cũ cộng đúng 24 PASS/1 FAIL; Figure 7.1 khớp kết quả lịch sử;
các phát hiện mất dữ liệu nhân sự và parser trong mục 7.4 đúng với nền code
cũ. Không đổi ngày hay kết quả lịch sử thành lần chạy mới.

## AI Audit Log WS6

Tên sheet được viết gọn dưới đây; tên đầy đủ trong workbook có tiền tố 1–4.

| Sheet/ô | Kết quả kiểm tra |
| --- | --- |
| Metadata C10 | 75 prompts là số khai báo sẵn, không có toàn bộ transcript để kiểm đếm độc lập; không tự cộng tool call thành prompt |
| Detailed A4:H15 | 12 entry liên tục, không có dòng trống giữa entry; cả 8 trường đều có nội dung |
| Detailed G4:G15 | Đủ bốn nhãn Critical Thinking, Contextualization, Creative Synthesis, Decision Ownership; nhãn đầy đủ không tự chứng minh người học đã xác nhận nội dung |
| Metadata C11 | COUNTA A4:A50 = 12, khớp dữ liệu nguồn và cached value |
| Metadata C12 | 12/75 = 0.16 = 16%, khớp cached value; nằm trong chỉ dẫn 10–20% của workbook |
| Metadata C13 | Ba dòng hallucination A4:A6; công thức COUNTA A4:A25 = 3, không đếm bảng tham khảo ở phần dưới |
| Metadata B22:B25 | Đếm độc lập và cached values khớp **2 Decomposition, 3 Pattern, 2 Abstraction, 5 Algorithms** |
| Detailed H4:H15 | Các tên file Java được nêu tồn tại trong src hiện tại; 12 ảnh nhúng có trong workbook, nhưng từng ảnh vẫn phải đối chiếu đúng phiên bản code |
| Hallucination F5 | Sai: khuyên `split(",", -1)` để bảo vệ ô có dấu phẩy/nháy. Cần thay bằng codec CSV dùng chung đã kiểm thử nhiều dòng; split chỉ giữ empty columns, không parse quote |
| Hallucination D4 | Khẳng định remove trong for-each luôn gây ConcurrentModificationException khi có ≥2 phần tử là quá tuyệt đối. Fail-fast không phải bảo đảm cho mọi cách duyệt/dừng; mô tả là thao tác không an toàn, dùng removeIf/Iterator.remove |
| Detailed hàng 15, entry 012; Checklist hàng 32 | Dẫn 24/25 là kết quả cũ. Giữ lịch sử nhưng bổ sung entry nghiệm thu mới có commit và log thực tế |
| Metadata A15:D18 | Chỉ liệt kê Antigravity và Gemini; chưa ghi việc hỗ trợ bằng Codex trong đợt hiện tại. Thêm công cụ/entry theo phiên thực tế, không thay toàn bộ lịch sử |
| Checklist C14:D18 | Số entry, DTC và số case khớp dữ liệu. Các dấu tick về chất lượng/reflection là tự đánh giá; không coi là chứng nhận tự động |
| Detailed row heights | Hàng 4–11 khoảng 200.1pt, thấp hơn hướng dẫn nội bộ khoảng 280pt; hàng 12–15 từ 279–345pt. Một số chữ/ảnh nhỏ, cần xem ở Excel 100% và kiểm tra phần cuối ô |

**Giới hạn renderer:** bản render Artifact Tool hiện hiển thị B22:B25 là
0 dù cached values gốc và đếm trực tiếp đều là 2/3/2/5. Đây là sai khác của
engine render/recalc đã quan sát, **không đủ căn cứ kết luận công thức trong
file nguồn sai**. Chưa chạy Microsoft Excel để chứng nhận recalculation.

Không thấy ô có data_type lỗi Excel được lưu trong các workbook; điều đó
không chứng minh mọi công thức đã được tính lại hoặc mọi evidence đúng.

## Final Audit Log và hồ sơ lịch sử

- Final có cùng 12 entry và phân bổ DTC 2/3/2/5, ba hallucination.
  **C11:C13 và B22:B25 không có cached values** trong bản lưu. Cần mở,
  recalculate, save bằng engine đã kiểm chứng rồi kiểm lại các số 12,
  16%, 3, 2/3/2/5; không sửa thành số hardcode để che lỗi cache.
- Final không có ảnh nhúng, trong khi WS6 có 12 ảnh. Cột Evidence có text
  nhưng không đồng nghĩa hồ sơ cuối đã đạt quy tắc minh chứng bằng ảnh của
  dự án. Nếu yêu cầu nhúng ảnh còn áp dụng, cần đưa minh chứng thật vào.
- Final Detailed: row heights khoảng 80.1–99.95pt cho nội dung dài, dễ
  bị cắt khi mở/print. Cần chỉnh chiều cao/wrap và xem lại thực tế.
- WS2–WS4 còn dẫn `AttendanceManager.java`; WS3–WS4 còn dẫn
  `ZonePolymorphismDemo.java` trong cột Evidence. Các tên này không có
  trong src hiện tại. Có thể là evidence lịch sử: phải gắn commit/file
  đúng thời điểm, không tự kết luận là bịa và không thay tên hàng loạt.
- WS5/WS6/Final không có tên Java bị thiếu trong cột Evidence khi đối
  chiếu cây src hiện tại. Điều này không xác thực nội dung ảnh hay prompt.
- Chưa xác minh mỗi tác giả đã tự giải thích được reflection trong vấn
  đáp. Không tự ký hoặc điền xác nhận thay người học.

## Nhiệm vụ hồ sơ kế tiếp

1. Lấy WS6 làm nền nội dung cho Final, loại mẫu Book và placeholder.
2. Cập nhật UML, persistence/state, CSV schema, menu export và giới hạn RBAC.
3. Chèn kết quả mới cùng commit từ docs/WS6, giữ dòng kết quả cũ có ngày.
4. Thêm entry hỗ trợ thật của đợt này; sửa Hallucination F5; người học tự
   xác nhận Human Delta và tổng prompt, không suy đoán hay ký thay.
5. Tính lại workbook bằng Excel, kiểm tra các ô nêu trên và ảnh evidence.
6. Render DOCX/PDF mới và kiểm tra tất cả các trang ở kích thước đọc được;
   chỉ sau đó mới đánh dấu hồ sơ sẵn sàng nộp.

## Dấu vết phiên bản đã đọc

| File | SHA-256 |
| --- | --- |
| Workshop6_Group2_Topic7.docx | `e86960e7eff98ea0c2f9f2fe1d829f24b281b87eeada8aeefffeff7e72672e66` |
| PRO192_Final_Report_Group2_SmartSite.docx | `383d0ace837390217c79a34df22aafb35242f4b659c72466536f5d2c4fbc5d87` |
| AI_AuditLog_Workshop_6_Group2_SE211059.xlsx | `2c5a8530d8a752a25432c2070fba561913fa8e98ef21c0bde5fc1351e25aa1bd` |
| AI_AuditLog_Final_Group2_SE211059.xlsx | `3711c87469742c8d8ee78a494818e01abc64cb23364855508d48b6b757c6f906` |
