# Kế hoạch triển khai ứng dụng Danh bạ

Trạng thái: **Đã triển khai theo yêu cầu tiếp tục code ngày 08/10/2026; xem README.md để chạy và demo.**

Nguồn yêu cầu: `DeGiuaKy_2026.pdf` (1 trang). Đã tiếp tục triển khai khi người dùng yêu cầu code dự án.

## 1. Yêu cầu trong đề

Xây dựng ứng dụng “Danh bạ” điện thoại. Bài kiểm tra thuộc học phần Lập trình di động, thời gian 45 phút.

| Hạng mục | Điểm trong đề | Hướng đáp ứng dự kiến |
| --- | ---: | --- |
| Thiết kế giao diện theo yêu cầu | 2 | Danh sách liên hệ và các màn hình thao tác rõ ràng |
| Sử dụng Custom Layout | 1 | Layout XML riêng cho từng dòng liên hệ, dùng adapter |
| Giao diện bổ sung khác | 0.5 | Màn hình chi tiết và hộp thoại xác nhận xóa |
| Thêm danh bạ mới | 1.5 | Nhập và lưu liên hệ |
| Chỉnh sửa thông tin liên hệ | 1.5 | Nạp thông tin cũ, sửa và lưu |
| Xóa danh bạ | 1.5 | Xác nhận trước khi xóa |
| Tìm kiếm | 0.5 | Tìm theo tên hoặc số điện thoại |
| Bộ lọc | 0.5 | Lọc theo nhóm liên hệ |
| Tính năng bổ sung khác | 1 | Yêu thích, mở trình quay số và soạn SMS |

Đề lặp số mục `1.1` ở phần giao diện; bảng trên phân biệt theo nội dung. Đề không kèm hình mẫu giao diện, không quy định trường dữ liệu, tiêu chí tìm kiếm, tiêu chí lọc hay cách lưu trữ. Các chi tiết ở phần sau là đề xuất để bạn duyệt, không phải yêu cầu được ghi sẵn trong PDF.

Yêu cầu nộp bài trong đề:

- Nộp source trên Microsoft Teams.
- Đặt package `com.midterm.<mssv>`, hoặc ghi thông tin MSSV, tên và lớp trong comment source.
- Video demo cần giới thiệu nhanh source và thể hiện đầy đủ các tính năng.
- Kế hoạch bao gồm chuẩn bị source và hướng dẫn demo; bạn tự thực hiện việc quay và nộp bài.

## 2. Ràng buộc và hiện trạng

- **Không dùng Jetpack Compose.** Dùng Android Views với layout XML.
- **Không dùng SQLite.** Không dùng Room hoặc thư viện lưu trữ dựa trên SQLite.
- Tiếp tục dùng Kotlin và AppCompat theo dự án hiện tại.
- Project hiện có package `com.midterm.danhba`, `MainActivity.kt` và `activity_main.xml`; chưa có nghiệp vụ danh bạ.
- Giữ cấu hình SDK hiện tại nếu build thành công; chỉ điều chỉnh khi có lỗi thực tế cần xử lý.
- Ứng dụng quản lý danh bạ riêng, không đọc hoặc sửa danh bạ hệ thống.
- Hoạt động offline, không cần tài khoản, server hay quyền đọc danh bạ.

## 3. Phạm vi chức năng dự kiến

### 3.1. Danh sách liên hệ

- Tiêu đề “Danh bạ”, ô tìm kiếm, bộ lọc nhóm và nút thêm.
- Danh sách bằng RecyclerView; mỗi dòng dùng `item_contact.xml` riêng.
- Một dòng hiển thị avatar chữ cái, tên, số điện thoại, nhóm và trạng thái yêu thích.
- Sắp xếp tên tăng dần; hiển thị thông báo khi chưa có liên hệ hoặc không có kết quả.
- Bấm một liên hệ để mở chi tiết.

### 3.2. Thêm và sửa

- Dùng chung một màn hình biểu mẫu cho hai chế độ.
- Trường bắt buộc: họ tên và số điện thoại.
- Trường tùy chọn: email và ghi chú.
- Nhóm: Gia đình, Bạn bè, Công việc, Khác; mặc định Khác.
- Cho phép đánh dấu yêu thích.
- Tên được bỏ khoảng trắng đầu/cuối; số điện thoại lưu dạng chuỗi để giữ số 0 đầu và dấu `+`.
- Kiểm tra tên/số không rỗng, định dạng số cơ bản và email nếu có nhập; báo lỗi cạnh trường.
- Đề xuất cho phép trùng tên nhưng không cho trùng số sau khi chuẩn hóa khoảng trắng, dấu gạch và dấu ngoặc. Khi sửa phải loại chính liên hệ đó khỏi kiểm tra trùng.
- Lưu thành công thì cập nhật danh sách; hủy thì không thay đổi dữ liệu. Cảnh báo khi rời biểu mẫu có thay đổi chưa lưu.

### 3.3. Chi tiết và xóa

- Hiển thị đầy đủ thông tin, nút sửa, xóa, yêu thích, gọi và SMS.
- Xóa có hộp thoại ghi rõ tên liên hệ; chỉ xóa sau khi xác nhận.
- Sau khi xóa, quay về danh sách và giữ điều kiện tìm kiếm/lọc khi phù hợp.

### 3.4. Tìm kiếm và lọc

- Tìm theo tên, không phân biệt hoa/thường và dấu tiếng Việt; tìm số theo chuỗi số đã chuẩn hóa.
- Bộ lọc nhóm gồm Tất cả, Gia đình, Bạn bè, Công việc, Khác.
- Có tùy chọn chỉ hiện liên hệ yêu thích.
- Tìm kiếm, nhóm và yêu thích kết hợp với nhau; xóa điều kiện sẽ khôi phục danh sách tương ứng.
- Thêm/sửa/xóa cập nhật kết quả đang hiển thị.

### 3.5. Chức năng bổ sung

- Bật/tắt yêu thích từ chi tiết và lưu trạng thái qua lần mở app sau.
- Nút gọi mở trình quay số bằng `ACTION_DIAL`, người dùng tự xác nhận cuộc gọi.
- Nút SMS mở ứng dụng soạn tin bằng `ACTION_SENDTO` với `smsto:`.
- Báo phù hợp nếu thiết bị không có ứng dụng xử lý gọi/SMS.
- Avatar chữ cái được tạo từ tên; chưa bổ sung chọn ảnh để giữ phạm vi gọn.

## 4. Lưu dữ liệu không dùng SQLite

Đề xuất dùng file **`contacts.json` trong bộ nhớ nội bộ của ứng dụng**.

- Đọc danh sách khi khởi động; giữ danh sách trong bộ nhớ trong lúc sử dụng.
- Sau mỗi thao tác thêm/sửa/xóa/yêu thích, ghi dữ liệu xuống file.
- Dùng JSON API có sẵn của Android; chưa cần thêm thư viện chuyển đổi JSON.
- Dùng `AtomicFile` để tránh thay file đang hợp lệ bằng dữ liệu ghi dở; thao tác đọc/ghi trên luồng nền.
- Chỉ thông báo lưu thành công sau khi ghi thành công. Nếu ghi thất bại, giữ trạng thái dữ liệu trước thao tác và cho phép thử lại.
- File chưa tồn tại: khởi tạo danh sách rỗng. File lỗi: báo lỗi, giữ file để xử lý, không tự ghi đè bằng danh sách rỗng.
- Dữ liệu tồn tại sau khi đóng/mở ứng dụng; xóa dữ liệu ứng dụng hoặc gỡ cài đặt sẽ làm mất dữ liệu cục bộ.
- Đây là phương án cho quy mô bài giữa kỳ; chưa làm đồng bộ cloud, nhập/xuất hay sao lưu thủ công.

Mô hình `Contact` dự kiến:

| Trường | Kiểu | Ý nghĩa |
| --- | --- | --- |
| id | String | UUID, định danh ổn định để sửa/xóa |
| name | String | Họ tên |
| phone | String | Số điện thoại |
| email | String | Email tùy chọn |
| group | String | Mã nhóm ổn định, tách khỏi nhãn tiếng Việt |
| note | String | Ghi chú tùy chọn |
| isFavorite | Boolean | Trạng thái yêu thích |

File JSON có `schemaVersion` và mảng `contacts` để dễ nhận biết định dạng dữ liệu.

## 5. Cấu trúc triển khai dự kiến

Giữ kiến trúc đơn giản, dễ trình bày khi demo:

```text
model/Contact.kt
data/ContactRepository.kt
ui/MainActivity.kt
ui/ContactAdapter.kt
ui/ContactFormActivity.kt
ui/ContactDetailActivity.kt
util/ContactValidator.kt
util/ContactSearch.kt
res/layout/activity_main.xml
res/layout/item_contact.xml
res/layout/activity_contact_form.xml
res/layout/activity_contact_detail.xml
```

- Repository phụ trách đọc/ghi JSON và thao tác dữ liệu.
- Activity phụ trách màn hình, nhận sự kiện và điều hướng.
- Adapter dùng layout dòng riêng để đáp ứng Custom Layout. Nếu giảng viên yêu cầu tự tạo lớp View/ViewGroup thay vì layout XML riêng, cần chốt lại cách hiểu trước khi triển khai.
- Truyền ID giữa các màn hình; lấy dữ liệu từ repository để tránh chỉnh sửa nhầm liên hệ khi danh sách đang lọc.
- Các màn hình dùng chung repository; tránh mỗi màn hình giữ một bản dữ liệu cũ rồi ghi đè nhau.
- Chuỗi giao diện đặt trong `strings.xml`, nội dung tiếng Việt; dùng theme và Material Views phù hợp với project.

## 6. Thứ tự thực hiện sau khi được duyệt

1. Chốt thông tin sinh viên, phạm vi chức năng và cách hiểu Custom Layout.
2. Tạo mô hình và repository lưu JSON; kiểm tra đọc/ghi và xử lý lỗi.
3. Xây dựng danh sách và layout dòng liên hệ.
4. Làm thêm/sửa và kiểm tra dữ liệu nhập.
5. Làm chi tiết, xóa có xác nhận và yêu thích.
6. Làm tìm kiếm, lọc nhóm và lọc yêu thích kết hợp.
7. Tích hợp mở trình quay số và soạn SMS.
8. Hoàn thiện giao diện, trạng thái rỗng, bàn phím, điều hướng và khôi phục biểu mẫu khi xoay màn hình.
9. Build, kiểm tra luồng chính, sửa lỗi và viết hướng dẫn chạy/demo.

## 7. Tiêu chí nghiệm thu

- Build debug thành công, chạy trên emulator hoặc thiết bị nếu môi trường có sẵn.
- Có thể thêm nhiều liên hệ; dữ liệu vẫn còn sau khi đóng và mở lại app.
- Sửa/xóa đúng liên hệ, kể cả khi đang tìm kiếm hoặc lọc.
- Hủy biểu mẫu/hủy xóa không thay đổi dữ liệu.
- Tên/số rỗng, số trùng và email sai được xử lý theo quy tắc đã chốt.
- Tìm tên có dấu/không dấu, tìm số, lọc nhóm và yêu thích hoạt động kết hợp.
- Trạng thái yêu thích tồn tại sau khi mở lại app.
- Nút gọi/SMS chuyển đúng số sang ứng dụng hệ thống.
- Danh sách rỗng và không có kết quả đều có thông báo rõ ràng.
- Xoay màn hình khi nhập không làm mất dữ liệu biểu mẫu; bàn phím và system bars không che nút thao tác.
- Kiểm tra có ý nghĩa cho chuẩn hóa/tìm kiếm, kiểm tra trùng và đọc/ghi JSON; kiểm tra luồng giao diện thực tế khi có thiết bị.
- Source không có Compose, SQLite, Room hoặc thư viện dùng SQLite để lưu danh bạ.
- Nếu môi trường thiếu SDK/emulator hoặc dependency, ghi rõ phần chưa kiểm chứng trong bàn giao.

## 8. Các điểm bạn cần chốt

Bạn có thể duyệt toàn bộ kế hoạch hoặc sửa các lựa chọn sau:

1. **Lưu dữ liệu:** file JSON nội bộ như đề xuất.
2. **Trường liên hệ:** tên, số điện thoại, email, nhóm, ghi chú, yêu thích.
3. **Bộ lọc:** nhóm và trạng thái yêu thích.
4. **Chức năng bổ sung:** yêu thích, mở trình quay số, mở soạn SMS.
5. **Số trùng:** chặn số đã có sau chuẩn hóa cơ bản.
6. **Thông tin sinh viên:** cung cấp MSSV, họ tên và lớp để đáp ứng yêu cầu đề. Trong khi chưa có, giữ package hiện tại; không tự điền thông tin giả.
7. **Custom Layout:** mặc định hiểu là layout XML riêng cho dòng danh bạ với adapter, do đề không mô tả chi tiết hơn.

**Đã bắt đầu triển khai theo yêu cầu tiếp tục code của người dùng. Thông tin sinh viên vẫn cần bổ sung trước khi nộp.**
