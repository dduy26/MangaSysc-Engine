# 📚 Manga Sync - Comic & Manga Platform

Hệ thống đọc truyện trực tuyến được thiết kế và xây dựng theo mô hình Client - Server với kiến trúc backend phân tầng (Layered Architecture). Dự án tích hợp hệ thống tự động cào và đồng bộ dữ liệu truyện từ các nguồn mở (MangaDex API), quản lý phiên bảo mật bằng JWT/Redis và hỗ trợ Rate Limiting chống tấn công DDoS/Spam.

---

## 🛠️ Tech Stack & Kiến trúc

### Backend
* **Ngôn ngữ & Framework:** Java 17, Spring Boot 3
* **Bảo mật & Xác thực:** Spring Security, JWT (JSON Web Token), cơ chế Blacklist Token qua Redis
* **Cơ sở dữ liệu:** PostgreSQL (Lưu trữ quan hệ chính), Redis (Quản lý phiên, Token & Rate Limiter)
* **ORM & Mapping:** Spring Data JPA, Hibernate, MapStruct
* **Tự động hóa:** Spring Task Scheduling (`@Scheduled`) phục vụ đồng bộ dữ liệu ngầm

### Frontend & Hạ tầng
* **Frontend:** React (Vite), Tailwind CSS
* **Reverse Proxy:** Nginx
* **Đóng gói & Triển khai:** Docker, Docker Compose

---

## 🚀 Các Tính Năng Nổi Bật (Backend Focus)

* **Hệ thống Quản lý Truyện & Chương:** Hỗ trợ CRUD thông tin truyện, quản lý danh sách chương, tác giả, thể loại và theo dõi lịch sử đọc, đánh dấu yêu thích.
* **Crawler & Data Sync Scheduler:**
  * Tích hợp dịch vụ cào truyện tự động từ MangaDex thông qua `MangadexImportService`.
  * Lập lịch tự động đồng bộ chương mới định kỳ qua `StoryScheduler` và ghi log kiểm soát qua `CrawlerLog`[cite: 11].
* **Bảo mật & Rate Limiting:**
  * Triển khai `JwtAuthFilter` cho các tài nguyên yêu cầu xác thực[cite: 11].
  * Sử dụng `RedisTokenService` lưu trữ và thu hồi token (Logout/Blacklist)[cite: 11].
  * `RateLimiterFilter` áp dụng thuật toán chặn và hạn chế tần suất gọi API, bảo vệ server khỏi spam request[cite: 11].
* **Tương tác Người dùng:** Hệ thống bình luận đa cấp, báo cáo bình luận vi phạm (`CommentReportController`)[cite: 11].

---

## 🗄️ Cấu trúc Cơ sở Dữ liệu Chính

* **`User`**: Quản lý thông tin tài khoản, mật khẩu băm (BCrypt) và vai trò (User/Admin)[cite: 11].
* **`Story`**: Thông tin truyện (tên, slug, tác giả, mô tả, trạng thái, ảnh bìa)[cite: 11].
* **`Chapter`**: Nội dung chương truyện, số thứ tự, liên kết ảnh lưu trữ[cite: 11].
* **`Comment` & `CommentReport`**: Bình luận của người đọc và báo cáo vi phạm cần duyệt[cite: 11].
* **`UserActivity`**: Lưu vết bookmark, lịch sử đọc truyện của người dùng[cite: 11].
* **`CrawlerLog`**: Nhật ký các lượt đồng bộ dữ liệu tự động[cite: 11].

---

## 💻 Hướng Dẫn Cài Đặt & Khởi Chạy

Toàn bộ hệ sinh thái đã được cấu hình sẵn môi trường container với Docker Compose[cite: 11].

### 1. Yêu cầu hệ thống
* Đã cài đặt [Docker](https://www.docker.com/) và [Docker Compose](https://docs.docker.com/compose/)[cite: 11].

### 2. Khởi chạy toàn bộ hệ thống (Recommended)
1. Clone repository về máy:
   ```bash
   git clone [https://github.com/dduy26/truyen-cloud.git](https://github.com/dduy26/truyen-cloud.git)
   cd truyen-cloud
