# Tổ chức model và quy tắc nghiệp vụ

Ngày rà soát 29/10/2026. Model được chỉnh theo hướng bảo vệ dữ liệu và hành
vi ngay trong đối tượng, giữ cấu trúc Java console hiện có. Không thêm
framework validation, Entity base dùng chung hay tầng quản lý state.

## Kiểm tra null và trim

`model/ModelValidation.java` là helper package-private dùng nội bộ các model:

```java
this.name = requiredText(name, "name");
this.role = requiredValue(role, "role");
```

- requiredText: trim một lần, từ chối null/blank và trả chuỗi đã chuẩn hóa.
- requiredValue: từ chối null cho enum hoặc collection bắt buộc.
- optionalText: null thành chuỗi rỗng và trim; chỉ dùng ở trường cho phép thiếu.
- phoneNumber/personCode: dùng chung quy tắc định dạng số điện thoại và mã nhân sự.
- timestamp/optionalDate: gọi DateTimeHelper và đổi lỗi parse thành lỗi field
  của model; DateTimeHelper vẫn là bộ codec ngày giờ dùng chung.

Constructor và setter dùng cùng helper, không lặp khối if null/trim/throw.
Ngày giờ vẫn phải đúng định dạng, không tự trim/chuyển ngày kiểu cũ ở CSV.
Ngày Visitor chưa biết được biểu diễn rõ bằng ô trống; console vẫn yêu cầu
ngày hợp lệ khi thêm khách. Không dùng chuỗi Unknown thay cho ngày.

ModelValidation đặt trong package model vì nó chia sẻ các quy tắc của mô
hình hiện tại, không phải bộ utility công khai cho mọi tầng. Helper không
đọc repository, in console hay quyết định vòng đời sự cố.

## Hợp đồng thông báo lỗi

Model dùng tiếng Anh thống nhất với menu và hệ exception hiện có. Không
ghép thêm tiền tố UI hoặc emoji vào message của model.

| Nhóm lỗi | Kiểu exception | Ví dụ |
| --- | --- | --- |
| Trường bắt buộc | DataValidationException | Invalid value for field [name]: Must not be blank. |
| Giá trị null | DataValidationException | Invalid value for field [severity]: Must not be null. |
| Điện thoại sai | InvalidPhoneNumberException, kế thừa DataValidationException | Invalid value for field [phoneNumber]: Must contain 10 digits and start with 0. |
| Ngày giờ sai tại model | DataValidationException | Invalid value for field [timestamp]: Must match yyyy-MM-dd HH:mm:ss and be a valid date and time. |
| Chuyển trạng thái sự cố sai | InvalidIncidentStateException | Invalid state for incident [I] (status=OPEN): Only ASSIGNED incidents can be resolved. |
| Chu kỳ điểm danh sai | AttendanceOrderException | Invalid attendance order for person [W1]: Cannot check out without a prior check-in. |

Các loại lỗi có ngữ cảnh khác nhau nên không ép tất cả thành cùng một câu.
Lỗi kỹ thuật khi gọi DateTimeHelper riêng vẫn là DateTimeParseException;
model bọc thành DataValidationException có tên field. Controller/View thêm
màu và nhãn hiển thị; Service không in thông báo.

## Hành vi nằm ở đâu

| Quy tắc | Trước refactor | Sau refactor |
| --- | --- | --- |
| Điện thoại hợp lệ | Chủ yếu kiểm tra ở PersonnelService | Person kiểm tra khi tạo/sửa, không thể bỏ qua bằng setter |
| Cập nhật tên và điện thoại | Service sửa hai field | Person.updateContactDetails kiểm tra đủ trước khi đổi cả hai |
| OPEN → ASSIGNED → RESOLVED | Service kiểm tra rồi gọi hai setter | Incident.assignTo/resolve giữ quy tắc và thay đổi nhất quán |
| Sự cố đã lưu có status/assignedTo mâu thuẫn | Loader và Facade kiểm tra | Constructor Incident kiểm tra ngay lúc tạo/reconstitute |
| Chu kỳ vào/ra và đúng khu vực khi ra | AttendanceService chứa FSM | AttendanceRecord.assertCanFollow kiểm tra với bản ghi trước |
| Whitelist và chứng chỉ | RestrictedZone.checkAccess | Giữ đa hình, chuẩn hóa và kiểm tra toàn bộ danh sách khi cấp/thay quyền |
| Tìm nhân sự/khu vực và ghi CSV | Service/Repository | Vẫn ở Service/Repository, không chuyển I/O vào model |

Service vẫn tìm đối tượng và kiểm tra sự tồn tại trong repository, ví dụ
người nhận sự cố phải nằm trong danh bạ. Model biết quy tắc của bản thân;
không tự truy cập registry để thực hiện tra cứu này.

Incident.report tạo sự cố OPEN. Constructor có status/assignedTo vẫn dùng
để nạp dữ liệu đã lưu, nhưng từ chối cặp giá trị mâu thuẫn. Không còn
setStatus/setAssignedTo công khai để bỏ qua assignTo/resolve.

Mã Person, vai trò theo subclass, mã Zone, mã Incident và định danh/tham
chiếu/loại thao tác của AttendanceRecord được giữ cố định sau khi tạo.
Điều này tránh đổi mã trong đối tượng nhưng khóa repository vẫn là mã cũ.
Timestamp vẫn có thể sửa bằng setter đã kiểm tra định dạng; không tuyên bố
AttendanceRecord hoàn toàn bất biến hoặc lịch sử đã được xác minh toàn bộ.

Mã nhân sự NONE và mã chứa dấu chấm phẩy bị từ chối ngay tại model vì
xung đột với biểu diễn whitelist CSV. Các danh sách quyền được sao chép
và kiểm tra toàn bộ trước khi thay thế, không để thay đổi dở dang khi lỗi.
recordCounter chỉ tăng sau khi repository chấp nhận bản ghi.

## Đánh giá rich domain

Theo cách phân biệt của [Martin Fowler](https://martinfowler.com/bliki/AnemicDomainModel.html),
domain model có hành vi và quy tắc nghiệp vụ, còn application service điều
phối công việc. Tách tầng không có nghĩa dồn hết hành vi khỏi model.

**Trước:** model đã có một phần hành vi đa hình nhưng còn nhiều setter cho
phép trạng thái sai, và các quy tắc chính nằm ở Service.

**Hiện tại:** mô hình có hành vi nghiệp vụ rõ hơn và kiểm tra được độc lập
với Service. Đây là hướng rich domain vừa đủ cho đồ án. Chưa gọi là domain
thuần tách hẳn hạ tầng: displayDetails/toCsvLine vẫn ở model theo cấu trúc
OOP đang dùng, mã/ngày vẫn biểu diễn bằng String, chưa có value object hoặc
aggregate chuyên biệt. Không thêm các lớp đó chỉ để có tên DDD.

## Kiểm chứng

**116/116 ca qua**, gồm 25 kịch bản WS6 gốc, 8 ca kiểm tra chuẩn hóa model
và 12 ca kiểm tra hành vi domain trực tiếp bên cạnh các bộ hồi quy trước.
Kiểm tra cả trường hợp không gọi qua Service để chứng minh model tự giữ
quy tắc: điện thoại/ngày sai, setter lỗi không đổi giá trị, contact update
không dở dang, assignment/resolve sai, trạng thái nạp CSV không nhất quán,
điểm danh sai chu kỳ và repository từ chối lưu không tiêu thụ mã REC.

Nhánh: codex/model-domain-rules. Commit chính:

- 83fb468: gom kiểm tra model nhóm nhân sự và khu vực.
- bd4cbd1: đưa chuyển trạng thái vào Incident/AttendanceRecord và làm mỏng Service.

Log: output/model-domain-tests.txt. Không thay dữ liệu thật hoặc file báo
cáo Word/Excel trong lượt chuẩn hóa này.
