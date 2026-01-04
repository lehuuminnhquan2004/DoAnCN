# DOANCN – Ứng dụng Chat Firebase (Android/Kotlin)

Ứng dụng chat 1-1 với đăng ký (OTP email), đăng nhập, tìm kiếm/kết bạn, lời mời kết bạn, danh bạ, trạng thái online/offline, chat realtime và trang hồ sơ (đổi avatar, đổi mật khẩu qua email, sửa tên/link ảnh).

## Tính năng chính
- Đăng ký tài khoản (OTP gửi email), đăng nhập, quên mật khẩu.
- Tìm kiếm người dùng (theo tên, tối thiểu 2 ký tự), gửi lời mời kết bạn, chấp nhận/từ chối.
- Danh sách bạn bè, mở chat 1-1; cập nhật online/offline và last seen, chấm xanh.
- Chat realtime với Firestore; input không bị che bởi bàn phím, tự ẩn sau gửi.
- Hồ sơ cá nhân: xem email/UID, đổi avatar (upload Storage hoặc dán link), sửa tên, đổi mật khẩu (gửi email reset), đăng xuất.

## Công nghệ
- Android, Kotlin, Material Components.
- Firebase: Auth, Firestore, Storage, Analytics.
- Glide (tải ảnh), JavaMail (gửi OTP – nên thay bằng backend an toàn).

## Cấu trúc dữ liệu Firestore
- `users/{uid}`: `uid`, `fullName`, `email`, `profileImage`, `online`, `lastSeen`, `createdAt`.
- `friend_requests/{from_to}`: `fromUid`, `toUid`, `status (pending|accepted|declined)`, `createdAt`.
- `friends/{uidA_uidB}`: `uid1`, `uid2`, `createdAt`.
- `chats/{uidA_uidB}`: `participants[2]`, `lastMessage`, `lastSenderId`, `updatedAt`; `messages/{autoId}`: `senderId`, `receiverId`, `message`, `timestamp`.
- Storage: `avatars/{uid}/{timestamp}.jpg`.

## Build & chạy
```bash
# Windows (đã cài JDK, Android SDK)
set JAVA_HOME=D:\Android\Android Studio\jbr   # chỉnh theo máy bạn
set PATH=%JAVA_HOME%\bin;%PATH%
.\gradlew.bat assembleDebug
# APK: app\build\outputs\apk\debug\app-debug.apk
```
Hoặc dùng Android Studio: Build > Build APK(s) (debug) hoặc Generate Signed Bundle/APK (release).

## Lưu ý bảo mật
- Không để mật khẩu SMTP/OTP trên client (JavaMail). Nên chuyển sang dịch vụ mail/OTP qua backend an toàn.
- Kiểm tra và cập nhật Firestore Security Rules cho phép người dùng chỉ sửa document của chính họ (users/{uid}) và hạn chế ghi dữ liệu nhạy cảm.

## Màn hình chính
- Đăng nhập/Đăng ký/Quên mật khẩu.
- Danh sách chat, Danh sách bạn bè, Lời mời kết bạn, Tìm kiếm bạn bè.
- Chat 1-1.
- Hồ sơ (đổi avatar, đổi tên/link ảnh, gửi email đổi mật khẩu, đăng xuất).

## Ghi chú UI
- Giao diện tông xanh, sử dụng toolbar, card, bottom navigation 64dp.
- Input chat: adjustResize để không bị che bởi bàn phím; ẩn bàn phím sau khi gửi.

