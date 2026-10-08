# Ứng dụng Danh bạ

Ứng dụng Android bằng Kotlin, Android Views và layout XML. Không dùng Compose, SQLite hoặc Room.

## Chức năng

- Danh sách RecyclerView với layout riêng `item_contact.xml`, avatar chữ cái và dấu yêu thích.
- Thêm, sửa, xem chi tiết và xóa có xác nhận.
- Họ tên, số điện thoại, email, nhóm, ghi chú và yêu thích.
- Tìm tên không phân biệt hoa/thường, dấu tiếng Việt; tìm số bỏ khoảng trắng, gạch, ngoặc.
- Lọc nhóm và yêu thích kết hợp với tìm kiếm.
- Chặn số trùng sau chuẩn hóa, giữ số 0 đầu và dấu +. Không quy đổi +84 thành 0.
- Mở trình quay số và ứng dụng SMS; không cần quyền gọi hay quyền truy cập danh bạ hệ thống.
- Giữ dữ liệu biểu mẫu khi xoay màn hình; cảnh báo khi bỏ thay đổi chưa lưu.

## Chạy dự án

1. Mở thư mục dự án bằng Android Studio, dùng JDK 21 theo cấu hình Gradle hiện tại.
2. Cài Android SDK 37.1 và chỉnh `local.properties` theo máy. Giữ nguyên các phiên bản dependency của dự án.
3. Sync Gradle, chọn thiết bị Android API 24 trở lên và Run.

Build và kiểm tra trên PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest
.\gradlew.bat :app:connectedDebugAndroidTest
```

APK debug: `app/build/outputs/apk/debug/app-debug.apk`.

## Dữ liệu và cấu trúc

Repository dùng chung lưu `contacts.json` trong bộ nhớ nội bộ, với `schemaVersion: 1`. Mọi đọc/ghi chạy tuần tự trên luồng nền và dùng `AtomicFile`. UI chỉ báo thành công sau khi ghi xong. Nếu file không đọc được, không tự thay bằng danh sách rỗng và không cho thêm dữ liệu đè lên file đó.

- `model/Contact.kt`: mô hình và mã nhóm.
- `data/ContactRepository.kt`: lưu trữ và thao tác dữ liệu.
- `MainActivity.kt`: danh sách, tìm kiếm và bộ lọc.
- `ui/`: adapter, biểu mẫu và chi tiết.
- `util/`: chuẩn hóa tìm kiếm, kiểm tra số và chống trùng.

Unit tests kiểm tra tìm kiếm và số điện thoại. Instrumentation tests kiểm tra lưu/đọc qua repository mới, sửa/xóa/yêu thích, file lỗi và rollback khi ghi thất bại. Các bài kiểm tra repository dùng thư mục cache riêng, không sửa danh bạ thật của ứng dụng.

## Kịch bản demo

1. Giới thiệu mô hình, repository và custom layout trong source.
2. Thêm “Đặng Ánh”, số “0901 234-567”, nhóm Gia đình, email hợp lệ, ghi chú và yêu thích.
3. Thêm một liên hệ khác thuộc nhóm Công việc.
4. Thử lưu tên rỗng, số không hợp lệ, email sai và số trùng “0901234567”.
5. Tìm “dang anh”, tìm “0901234”; kết hợp lọc Gia đình và chỉ hiện yêu thích.
6. Mở chi tiết, sửa tên/nhóm/số; kiểm tra danh sách cập nhật theo bộ lọc.
7. Bật/tắt yêu thích; mở gọi và SMS, kiểm tra số được chuyển đúng.
8. Thay đổi biểu mẫu rồi quay lại; thử tiếp tục nhập và bỏ thay đổi. Xoay màn hình khi nhập.
9. Thử hủy xóa, sau đó xác nhận xóa.
10. Đóng/mở lại ứng dụng để kiểm tra dữ liệu vẫn còn; thử tìm không có kết quả.

## Thông tin nộp bài

Package hiện tại là `com.midterm.danhba`. Cần bổ sung MSSV, họ tên và lớp thật trong comment source hoặc đổi package theo đề trước khi nộp. Chưa có thông tin sinh viên nên source không điền thông tin giả. Quay video demo và nộp source trên Microsoft Teams theo đề.

## Kết quả kiểm chứng ngày 08/10/2026

- Build debug và APK instrumentation test thành công.
- 6 unit test vượt qua (5 bài kiểm tra nghiệp vụ, 1 bài mẫu có sẵn).
- Chưa chạy instrumentation test hoặc demo trên thiết bị: ADB không khởi động được do lỗi tạo thư mục cấu hình trong môi trường hiện tại.
- Android lint chưa chạy hoàn tất: thiếu lint-gradle 32.2.1 và môi trường chặn kết nối tải từ Google Maven. Không bỏ qua hoặc tắt các quy tắc lint.
- compileSdk được nâng từ 36.1 lên 37.1 vì AndroidX Core 1.19.1 yêu cầu API 37; targetSdk vẫn 36, minSdk vẫn 24. AGP 9.2.1 có cảnh báo chỉ được kiểm chứng tới SDK 37.0, nhưng build với SDK 37.1 hiện có đã thành công.
