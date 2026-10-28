# Tài liệu triển khai — Meeting Room Booking Service

> **Dành cho AI/Developer triển khai:** Đọc toàn bộ file này cùng `AGENT.md` (coding rules) trước khi viết code.
> Khi `AGENT.md` và file này mâu thuẫn, ưu tiên `AGENT.md` về coding convention, ưu tiên file này về nghiệp vụ.
> Những chỗ ghi **[QUYẾT ĐỊNH]** là quyết định thiết kế đã chốt — không tự đổi, và phải giải thích lý do trong `README.md`.

## 1. Bối cảnh & mục đích

Công ty có khoảng **10 phòng họp** và **200 nhân viên**. Hiện nay nhân viên đặt phòng bằng cách nhắn tin cho nhau nên thường xuyên **trùng lịch**. Cần một backend REST API để:

- Quản lý phòng họp (CRUD, trạng thái, tiện ích).
- Đặt / đổi lịch / huỷ phòng, **tuyệt đối không cho phép trùng lịch** kể cả khi nhiều người đặt đồng thời.
- Xem lịch (theo phòng, theo ngày toàn công ty, lịch của tôi).
- Tìm phòng trống theo khoảng thời gian, sức chứa, tiện ích.
- Phân quyền **người dùng thường (EMPLOYEE)** và **quản trị viên (ADMIN)**.

Hệ thống phải được thiết kế như **sẽ đưa vào sử dụng thật**: có bảo mật, validation, xử lý đồng thời, migration, logging, healthcheck, tài liệu API.

### 1.1 Quy mô & hệ quả thiết kế

| Chỉ số | Giá trị ước lượng | Hệ quả |
|---|---|---|
| Số phòng | ~10 | Không cần sharding/cache phức tạp |
| Số user | ~200 | Quản lý user nội bộ, không cần SSO ở phiên bản này |
| Booking / ngày | ~50–150 | 1 instance + 1 PostgreSQL là đủ |
| Đọc >> ghi | Xem lịch/tìm phòng nhiều hơn đặt | Index đúng, query theo khoảng thời gian |

> **[QUYẾT ĐỊNH]** Không dùng Redis, Kafka, microservice. Với quy mô này đó là over-engineering. Chỉ dùng **Spring Boot monolith + PostgreSQL**. Ghi rõ lý do vào README.

### 1.2 Ngoài phạm vi (out of scope)

- Không gửi email/SMS/push thật (chỉ phát domain event nội bộ + log, để mở rộng sau).
- Không tích hợp Google/Outlook Calendar, không SSO/LDAP.
- Không có frontend (chỉ REST API + Swagger UI).
- Không đặt lịch lặp lại (recurring) — ghi vào mục "Hướng mở rộng" của README.
- Không quản lý thanh toán / chi phí phòng.

## 2. Tech stack

| Hạng mục | Lựa chọn | Lý do ngắn gọn |
|---|---|---|
| Ngôn ngữ / Framework | Java 21, Spring Boot 3.x | Theo AGENT.md |
| Database | PostgreSQL 16 | Hỗ trợ **exclusion constraint** chống trùng lịch ở tầng DB |
| Migration | Flyway | Schema versioned, chạy tự động khi start |
| ORM | Spring Data JPA (Hibernate) | |
| Security | Spring Security + JWT (access + refresh) | Stateless, phù hợp REST |
| Mapping | MapStruct | Theo AGENT.md |
| Boilerplate | Lombok | Theo AGENT.md |
| API docs | springdoc-openapi (Swagger UI) | |
| Validation | Jakarta Bean Validation | |
| Observability | Spring Boot Actuator (`/actuator/health`) + Logback rolling file (theo ngày) + log JSON có correlationId | Dễ debug, đóng gói log theo ngày, không cần ELK ở giai đoạn này |
| Container | Dockerfile multi-stage + docker-compose | Yêu cầu đề bài |

Được phép thêm thư viện khác nếu thật sự cần, nhưng **mỗi thư viện thêm vào phải có lý do trong README**.

## 3. Actors & phân quyền

| Role | Quyền |
|---|---|
| `EMPLOYEE` | Đăng nhập; xem danh sách phòng; xem lịch; tìm phòng trống; đặt phòng; xem / sửa / huỷ **booking của chính mình**; xem thông tin cá nhân; đổi mật khẩu |
| `ADMIN` | Tất cả quyền của EMPLOYEE + CRUD phòng, tiện ích; đổi trạng thái phòng; xem **mọi** booking; huỷ **bất kỳ** booking (bắt buộc ghi lý do); quản lý user (tạo, khoá/mở khoá, reset mật khẩu, đổi role) |

> **[QUYẾT ĐỊNH]** Không có API tự đăng ký (public signup). Admin tạo tài khoản cho nhân viên vì đây là hệ thống nội bộ. Một ADMIN mặc định được seed khi khởi động lần đầu từ biến môi trường (`APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD` — không có giá trị mặc định trong code hay compose, xem mục 8), bắt buộc đổi mật khẩu ở lần đăng nhập đầu (`must_change_password = true`).

## 4. Domain Model

### 4.1 Quy ước chung (áp dụng cho mọi bảng/entity)

- **Mọi trường thời gian dùng kiểu `Long`** = epoch **milliseconds (UTC)** — cả trong entity, DTO và cột DB (`BIGINT`). (Theo AGENT.md mục 6.)
- Trường `name`, `description`, `title` mang nội dung hiển thị **do hệ thống/admin quản lý** thì phải **localize `{vi, en}`**: lưu thành 2 cột `xxx_vi`, `xxx_en`; response trả về object `{ "vi": "...", "en": "..." }`.
- Thông điệp lỗi trả về theo header `Accept-Language` (`vi` mặc định, `en`), lấy từ `messages_vi.properties` / `messages_en.properties`.
- Khoá chính `BIGINT` sinh bằng sequence/identity. Mọi bảng có `created_at`, `updated_at` (BIGINT epoch ms).
- Dùng **soft state** (status) thay vì xoá cứng với User, Room, Booking.
- **[QUYẾT ĐỊNH]** `Booking.title` là văn bản nhân viên tự nhập (ví dụ "Họp sprint planning") nên lưu **một cột duy nhất**, không localize. Chỉ localize nội dung do hệ thống quản lý.

### 4.2 Entity: User

| Trường | Kiểu | Ràng buộc | Ghi chú |
|---|---|---|---|
| user_id | BIGINT | PK | |
| email | VARCHAR(150) | UNIQUE NOT NULL | Dùng làm username, lưu lowercase |
| password_hash | VARCHAR(100) | NOT NULL | BCrypt |
| full_name | VARCHAR(100) | NOT NULL | |
| department | VARCHAR(100) | | |
| role | VARCHAR(15) | NOT NULL | EMPLOYEE / ADMIN |
| status | VARCHAR(10) | NOT NULL | ACTIVE / LOCKED |
| must_change_password | BOOLEAN | NOT NULL | Mặc định true khi admin tạo / reset |
| failed_login_count | INT | NOT NULL, mặc định 0 | Số lần đăng nhập sai liên tiếp (mục 6.5); về 0 khi đăng nhập thành công |
| locked_until | BIGINT | NULL | Epoch ms UTC; khoá **tạm** do đăng nhập sai cho tới thời điểm này (mục 6.5) |
| created_at / updated_at | BIGINT | NOT NULL | |

> **[QUYẾT ĐỊNH]** Bảng User đặt tên **`app_user`** (`@Table(name = "app_user")`), không dùng `user` vì đó là từ khoá dành riêng của PostgreSQL. `status = LOCKED` là khoá thủ công do ADMIN (không tự hết hạn); `locked_until` là khoá tạm do đăng nhập sai (tự hết hạn). User bị từ chối đăng nhập nếu `status = LOCKED` **hoặc** `locked_until > now`.

### 4.3 Entity: Room (aggregate root cho phòng)

| Trường | Kiểu | Ràng buộc | Ghi chú |
|---|---|---|---|
| room_id | BIGINT | PK | |
| code | VARCHAR(20) | UNIQUE NOT NULL | VD `HN-3F-01` |
| name_vi / name_en | VARCHAR(100) | NOT NULL | Localize |
| description_vi / description_en | VARCHAR(500) | | Localize |
| capacity | INT | NOT NULL, > 0 | Sức chứa tối đa |
| floor | VARCHAR(10) | | Tầng / vị trí |
| status | VARCHAR(15) | NOT NULL | ACTIVE / MAINTENANCE / INACTIVE |
| created_at / updated_at | BIGINT | NOT NULL | |

### 4.4 Entity: Equipment & RoomEquipment

**Equipment** (danh mục tiện ích)

| Trường | Kiểu | Ràng buộc |
|---|---|---|
| equipment_id | BIGINT | PK |
| code | VARCHAR(30) | UNIQUE NOT NULL (VD `PROJECTOR`, `VIDEO_CONF`, `WHITEBOARD`) |
| name_vi / name_en | VARCHAR(100) | NOT NULL |

**RoomEquipment** (n–n)

| Trường | Kiểu | Ràng buộc |
|---|---|---|
| room_id | BIGINT | PK, FK → Room |
| equipment_id | BIGINT | PK, FK → Equipment |

### 4.5 Entity: Booking (aggregate root cho đặt phòng)

| Trường | Kiểu | Ràng buộc | Ghi chú |
|---|---|---|---|
| booking_id | BIGINT | PK | |
| room_id | BIGINT | NOT NULL, FK → Room | |
| booked_by | BIGINT | NOT NULL, FK → User | Người đặt (chủ booking) |
| title | VARCHAR(200) | NOT NULL | Tiêu đề cuộc họp |
| description | VARCHAR(1000) | | |
| attendee_count | INT | NOT NULL, > 0 | Phải ≤ `Room.capacity` |
| start_time | BIGINT | NOT NULL | Epoch ms UTC, **bao gồm** |
| end_time | BIGINT | NOT NULL, > start_time | Epoch ms UTC, **không bao gồm** |
| status | VARCHAR(15) | NOT NULL | CONFIRMED / CANCELLED |
| cancelled_by | BIGINT | NULL, FK → User | |
| cancel_reason | VARCHAR(255) | NULL | Bắt buộc nếu ADMIN huỷ booking của người khác |
| cancelled_at | BIGINT | NULL | |
| version | BIGINT | NOT NULL | `@Version` — optimistic lock khi sửa |
| created_at / updated_at | BIGINT | NOT NULL | |

> **[QUYẾT ĐỊNH]** Khoảng thời gian là **nửa mở `[start_time, end_time)`**: họp 09:00–10:00 và họp 10:00–11:00 **không** trùng nhau.

### 4.6 Entity: BookingHistory (audit)

| Trường | Kiểu |
|---|---|
| id | BIGINT PK |
| booking_id | BIGINT FK |
| action | CREATED / RESCHEDULED / CANCELLED |
| old_start_time / old_end_time | BIGINT NULL |
| new_start_time / new_end_time | BIGINT NULL |
| actor_id | BIGINT FK → User |
| note | VARCHAR(255) |
| created_at | BIGINT |

> Lưu vĩnh viễn, phục vụ truy vết tranh chấp "ai huỷ lịch của tôi". Truy xuất qua `GET /bookings/{bookingId}/history` (mục 5.3).

### 4.7 Chống trùng lịch — QUAN TRỌNG NHẤT

Chỉ kiểm tra ở tầng service là **chưa đủ** vì 2 request đồng thời có thể cùng qua bước kiểm tra. Phải chặn ở **tầng database**:

```sql
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE booking
  ADD CONSTRAINT ex_booking_no_overlap
  EXCLUDE USING gist (
    room_id WITH =,
    int8range(start_time, end_time, '[)') WITH &&
  ) WHERE (status = 'CONFIRMED');
```

Tương tự, một user không thể ở 2 phòng cùng lúc cũng phải được chặn ở tầng DB (nếu chỉ kiểm tra ở service thì 2 request đồng thời của cùng một user vào 2 phòng khác nhau vẫn cùng qua được):

```sql
ALTER TABLE booking
  ADD CONSTRAINT ex_booking_user_no_overlap
  EXCLUDE USING gist (
    booked_by WITH =,
    int8range(start_time, end_time, '[)') WITH &&
  ) WHERE (status = 'CONFIRMED');
```

Quy trình ở service:

1. Validate các rule nghiệp vụ (mục 6) và kiểm tra trùng bằng query để trả lỗi thân thiện sớm.
2. `saveAndFlush()` booking. Nếu DB ném vi phạm exclusion constraint (`DataIntegrityViolationException`, SQLSTATE `23P01`), **đọc tên constraint** từ cause chain (`ConstraintViolationException.getConstraintName()`) rồi map: `ex_booking_no_overlap` → `409 ROOM_ALREADY_BOOKED`; `ex_booking_user_no_overlap` → `409 USER_DOUBLE_BOOKED`. Mapping này đặt **một chỗ** (ví dụ `ConstraintViolationTranslator` dùng chung cho tạo và đổi lịch). Vi phạm constraint khác (ví dụ trùng email) **không** được map nhầm thành lỗi trùng lịch.
3. Booking đã `CANCELLED` **không** tham gia constraint nên slot được giải phóng ngay.

> **[QUYẾT ĐỊNH]** Dùng exclusion constraint của PostgreSQL thay vì khoá ứng dụng (`synchronized`, distributed lock). Lý do: đúng ngay cả khi chạy nhiều instance, không phụ thuộc thành phần ngoài, và là "chốt chặn cuối" không thể bị bypass bởi lỗi code.

> **[QUYẾT ĐỊNH]** Quota 20 booking tương lai (mục 6.1) chỉ kiểm tra ở service, không có constraint DB. Hai request đồng thời của cùng một user có thể vượt quota tối đa vài booking; chấp nhận vì không ảnh hưởng tính đúng đắn của lịch phòng. Ghi vào README, mục "Giới hạn đã biết".

### 4.8 Index đề xuất

- `booking(room_id, start_time, end_time) WHERE status = 'CONFIRMED'` — xem lịch / tìm phòng trống.
- `booking(booked_by, start_time DESC)` — "lịch của tôi", và kiểm tra user trùng lịch.
- `room(status)`, `app_user(email)` (đã unique).

## 5. API Contract

Base path: `/api/v1`. Mọi API (trừ login/refresh/health/swagger) yêu cầu header `Authorization: Bearer <accessToken>`.

### 5.1 Auth & User

```
POST   /auth/login                      — đăng nhập, trả access + refresh token
POST   /auth/refresh                    — đổi refresh token lấy access token mới
POST   /auth/change-password            — đổi mật khẩu của chính mình
GET    /users/me                        — thông tin cá nhân

# ADMIN
POST   /admin/users                     — tạo user
GET    /admin/users                     — danh sách user (phân trang, lọc theo keyword/role/status)
GET    /admin/users/{userId}            — chi tiết user
PATCH  /admin/users/{userId}/status     — khoá / mở khoá
PATCH  /admin/users/{userId}/role       — đổi role
POST   /admin/users/{userId}/reset-password — reset mật khẩu
```

### 5.2 Room & Equipment

```
GET    /rooms                           — danh sách phòng (phân trang; lọc: keyword, minCapacity, equipmentIds, status)
GET    /rooms/{roomId}                  — chi tiết phòng + tiện ích
GET    /equipments                      — danh mục tiện ích (phân trang)

# ADMIN
POST   /admin/rooms                     — tạo phòng
PUT    /admin/rooms/{roomId}            — cập nhật phòng
PATCH  /admin/rooms/{roomId}/status     — đổi trạng thái (ACTIVE/MAINTENANCE/INACTIVE)
POST   /admin/equipments                — tạo tiện ích
PUT    /admin/equipments/{equipmentId}  — sửa tiện ích
```

### 5.3 Booking

```
POST   /bookings                        — đặt phòng
GET    /bookings/{bookingId}            — chi tiết (chủ booking hoặc ADMIN)
GET    /bookings/me                     — booking của tôi (phân trang; lọc: status, fromTime, toTime; mặc định sắp xếp start_time DESC)
PUT    /bookings/{bookingId}            — đổi lịch/thông tin (chủ booking hoặc ADMIN)
POST   /bookings/{bookingId}/cancel     — huỷ booking (body: reason)
GET    /bookings/{bookingId}/history    — lịch sử thay đổi của booking (chủ booking hoặc ADMIN; phân trang)

# ADMIN
GET    /admin/bookings                  — mọi booking (phân trang; lọc: roomId, userId, status, fromTime, toTime)
```

> **[QUYẾT ĐỊNH]** Huỷ dùng `POST .../cancel` (kèm reason + lưu lịch sử) thay vì `DELETE`, vì đây là chuyển trạng thái có audit chứ không xoá dữ liệu.

**Lịch sử booking** (`GET /bookings/{bookingId}/history`):

- Chủ booking hoặc ADMIN được xem; người khác nhận `403`; booking không tồn tại nhận `404`. Kiểm tra quyền sở hữu ở **service**.
- Phân trang mặc định `page=0`, `size=10`, sắp xếp `created_at DESC`.
- Mỗi dòng gồm: `historyId`, `action` (`CREATED` / `RESCHEDULED` / `CANCELLED`), `oldStartTime`, `oldEndTime`, `newStartTime`, `newEndTime`, `actor` (`{ userId, fullName }`), `note`, `createdAt` (các trường thời gian là `Long`). Không trả entity `BookingHistory` trực tiếp.
- Xử lý ở `BookingQueryService`, map bằng `BookingMapper`.

### 5.4 Lịch & tìm phòng trống

```
GET    /rooms/{roomId}/calendar?fromTime=&toTime=   — lịch 1 phòng trong khoảng thời gian
GET    /calendar?fromTime=&toTime=                  — lịch toàn bộ phòng (dạng nhóm theo phòng)
GET    /rooms/available?startTime=&endTime=&minCapacity=&equipmentIds=  — tìm phòng trống (phân trang)
```

- `calendar` **không phân trang** nhưng bị giới hạn khoảng thời gian tối đa **31 ngày** (vượt → `400 CALENDAR_RANGE_TOO_LARGE`). Đây là lý do kỹ thuật hợp lệ để không phân trang (kết quả bị chặn theo thời gian và ≤ ~10 phòng); ghi rõ trong README.
- `rooms/available`: chỉ trả phòng `ACTIVE`, đủ `minCapacity`, có đủ **tất cả** `equipmentIds`, và **không có booking CONFIRMED** giao với `[startTime, endTime)`. Sắp xếp theo sức chứa nhỏ nhất đủ dùng trước (tránh dùng phòng to cho họp ít người).
- Calendar cho EMPLOYEE hiển thị đủ `title` và người đặt để phối hợp (**[QUYẾT ĐỊNH]** công ty nội bộ, minh bạch lịch họp; nếu cần riêng tư thì thêm cờ `private` ở phase sau).

### 5.5 Quy ước request/response

**Response thành công** (đối tượng đơn):

```json
{
  "bookingId": 5001,
  "roomId": 3,
  "roomName": { "vi": "Phòng Hà Nội", "en": "Hanoi Room" },
  "title": "Sprint planning",
  "attendeeCount": 8,
  "startTime": 1788321600000,
  "endTime": 1788325200000,
  "status": "CONFIRMED"
}
```

**Response danh sách có phân trang** (dùng `Page<T>` của Spring Data, mặc định `page=0`, `size=10`, tối đa `size=100`):

```json
{
  "content": [ ... ],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5
}
```

**Response lỗi thống nhất** (xử lý bằng `@RestControllerAdvice`):

```json
{
  "code": "ROOM_ALREADY_BOOKED",
  "message": "Phòng đã được đặt trong khung giờ này",
  "timestamp": 1788321600000,
  "path": "/api/v1/bookings",
  "details": []
}
```

| HTTP | Khi nào |
|---|---|
| 400 | Validation sai, vi phạm rule nghiệp vụ dạng input (ngoài giờ làm việc, quá 4 giờ, ...) |
| 401 | Chưa đăng nhập / token hết hạn |
| 403 | Không đủ quyền (EMPLOYEE sửa booking người khác...) |
| 404 | Không tìm thấy tài nguyên |
| 409 | Trùng lịch phòng, user trùng lịch, sai trạng thái, `OptimisticLock` |
| 500 | Lỗi không lường trước (không lộ stacktrace) |

### 5.6 Ví dụ — Đặt phòng

```json
// POST /api/v1/bookings
{
  "roomId": 3,
  "title": "Sprint planning",
  "description": "Họp kế hoạch sprint 24",
  "attendeeCount": 8,
  "startTime": 1788321600000,
  "endTime": 1788325200000
}

// Response 201
{
  "bookingId": 5001,
  "roomId": 3,
  "roomName": { "vi": "Phòng Hà Nội", "en": "Hanoi Room" },
  "title": "Sprint planning",
  "attendeeCount": 8,
  "startTime": 1788321600000,
  "endTime": 1788325200000,
  "status": "CONFIRMED",
  "bookedBy": { "userId": 17, "fullName": "Nguyễn Văn A" }
}

// Response 409 (trùng lịch)
{
  "code": "ROOM_ALREADY_BOOKED",
  "message": "Phòng đã được đặt trong khung giờ này",
  "timestamp": 1788321600123,
  "path": "/api/v1/bookings",
  "details": []
}
```

## 6. Business Rules

### 6.1 Rule đặt phòng (cấu hình được qua `application.yml`)

| Rule | Giá trị mặc định | Mã lỗi |
|---|---|---|
| Phòng phải ở trạng thái `ACTIVE` | — | `ROOM_NOT_AVAILABLE` |
| `attendeeCount` ≤ `Room.capacity` | — | `ATTENDEE_EXCEEDS_CAPACITY` |
| `endTime > startTime` | — | `INVALID_TIME_RANGE` |
| Thời lượng tối thiểu / tối đa | 15 phút / 4 giờ | `INVALID_DURATION` |
| Thời gian phải khớp bước | Bội số 15 phút | `INVALID_TIME_SLOT` |
| Không đặt trong quá khứ | `startTime ≥ now` | `BOOKING_IN_THE_PAST` |
| Đặt trước tối đa | 60 ngày | `BOOKING_TOO_FAR_AHEAD` |
| Trong giờ làm việc | 08:00–18:00, giờ `Asia/Ho_Chi_Minh` | `OUTSIDE_WORKING_HOURS` |
| Ngày làm việc | Thứ 2 – Thứ 6 | `OUTSIDE_WORKING_DAYS` |
| Một booking không vắt qua nhiều ngày | `start`, `end` cùng ngày (theo múi giờ công ty) | `MULTI_DAY_NOT_ALLOWED` |
| Phòng không trùng booking CONFIRMED khác | — | `ROOM_ALREADY_BOOKED` |
| **Một user không thể ở 2 phòng cùng lúc** (không có 2 booking CONFIRMED của cùng `booked_by` giao nhau; chốt chặn cuối là constraint `ex_booking_user_no_overlap`, mục 4.7) | — | `USER_DOUBLE_BOOKED` |
| Giới hạn booking tương lai đang hoạt động của 1 user | 20 | `BOOKING_QUOTA_EXCEEDED` |

> **[QUYẾT ĐỊNH]** Múi giờ nghiệp vụ cố định `Asia/Ho_Chi_Minh` (cấu hình được), DB và API đều dùng epoch ms UTC nên không phụ thuộc múi giờ server/container. Giờ làm việc được quy đổi ở service. Lý do: tránh lỗi lệch múi giờ khi Docker chạy UTC.

> **[QUYẾT ĐỊNH]** Quota (20 booking tương lai) chống một người "chiếm" phòng hàng loạt. Admin không bị giới hạn quota.

### 6.2 Sửa / đổi lịch

- Chỉ **chủ booking** hoặc **ADMIN** được sửa.
- Chỉ sửa được booking `CONFIRMED` và **chưa bắt đầu** (`now < start_time`). Booking đang diễn ra hoặc đã qua → `409 BOOKING_NOT_MODIFIABLE`. (Ngoại lệ: không có; muốn kéo dài thì đặt booking mới.)
- Đổi lịch phải qua lại **toàn bộ rule 6.1** (loại trừ chính booking đang sửa khi kiểm tra trùng).
- Dùng `@Version` (optimistic lock): sửa đồng thời → `409 BOOKING_CONCURRENT_MODIFICATION`.
- Ghi `BookingHistory` action `RESCHEDULED`.

### 6.3 Huỷ booking — dùng Strategy + Factory theo role

| Người huỷ | Điều kiện | Lý do bắt buộc? |
|---|---|---|
| Chủ booking (EMPLOYEE) | Booking `CONFIRMED` và `now < start_time` | Không |
| ADMIN huỷ booking của chính mình | Như trên | Không |
| ADMIN huỷ booking của người khác | Bất kỳ lúc nào trước khi kết thúc (`now < end_time`) | **Có** |

- Huỷ lần 2 → `409 BOOKING_ALREADY_CANCELLED`.
- Sau huỷ: `status = CANCELLED`, lưu `cancelled_by`, `cancel_reason`, `cancelled_at`, ghi `BookingHistory`, phát event `booking.cancelled` (để sau này gửi thông báo cho chủ booking nếu bị admin huỷ).

### 6.4 Quản lý phòng

- `code` duy nhất.
- Không xoá cứng phòng đã có booking; dùng `INACTIVE`.
- Chuyển phòng sang `MAINTENANCE` hoặc `INACTIVE` khi còn **booking CONFIRMED trong tương lai**:
  - Mặc định → `409 ROOM_HAS_FUTURE_BOOKINGS` (kèm số lượng).
  - Nếu request có `cancelFutureBookings = true` và `reason` → tự động huỷ toàn bộ booking tương lai của phòng đó trong **một transaction**, ghi history + phát event cho từng booking.
- Giảm `capacity` xuống thấp hơn `attendee_count` của booking tương lai → `409 CAPACITY_CONFLICTS_WITH_BOOKINGS`.

### 6.5 Quản lý user

- Email duy nhất, lưu lowercase.
- Mật khẩu tối thiểu 8 ký tự, có chữ và số.
- User `LOCKED` không đăng nhập được và token hiện có bị từ chối ở request kế tiếp (kiểm tra status khi xác thực).
- Admin không thể tự khoá / tự hạ quyền chính mình (tránh hệ thống mất admin) → `409 CANNOT_MODIFY_SELF`.
- Khoá user **không** tự huỷ booking tương lai của user đó (admin huỷ tay nếu cần) — ghi vào README.
- Đăng nhập sai: phản hồi chung `INVALID_CREDENTIALS` (không tiết lộ email có tồn tại hay không). Giới hạn **5 lần sai liên tiếp → khoá tạm 15 phút** (đơn giản: lưu `failed_login_count`, `locked_until` trên User — cột đã khai báo ở mục 4.2; reset `failed_login_count = 0` khi đăng nhập thành công hoặc khi admin reset mật khẩu / mở khoá).

## 7. Kiến trúc & thiết kế code

### 7.1 Kiến trúc phân tầng (theo AGENT.md mục 8)

`Controller → Service → Repository` — không bypass tầng. Controller không chứa business logic, không gọi Repository.

### 7.2 Cấu trúc package đề xuất

```
com.company.roombooking
├── RoomBookingApplication
├── config            # SecurityConfig, OpenApiConfig, JacksonConfig, BookingProperties (@ConfigurationProperties), I18nConfig
├── common
│   ├── dto           # PageResponse, ErrorResponse, LocalizedText
│   ├── exception     # BusinessException, ErrorCode (enum), GlobalExceptionHandler
│   ├── util          # TimeUtils (epoch ↔ ZonedDateTime), SecurityUtils
│   └── base          # BaseEntity (id, createdAt, updatedAt)
├── auth              # controller, service, dto, jwt (JwtService, JwtAuthFilter)
├── user              # controller, service, repository, entity, dto, mapper
├── room              # controller, service, repository, entity, dto, mapper, specification
├── booking
│   ├── controller
│   ├── service       # BookingService, BookingQueryService, CalendarService, AvailabilityService
│   ├── repository
│   ├── entity
│   ├── dto
│   ├── mapper
│   ├── rule          # BookingRule + các implementation (Chain of Responsibility / Strategy)
│   ├── cancel        # CancelStrategy + Factory
│   └── event         # BookingCreatedEvent, BookingCancelledEvent, listeners
└── resources
    ├── db/migration  # V1__..., V2__...
    ├── messages_vi.properties / messages_en.properties
    └── application.yml (+ application-docker.yml)
```

### 7.3 Design pattern áp dụng (chỉ dùng khi thật sự đơn giản hoá code)

| Pattern | Áp dụng ở đâu | Lý do |
|---|---|---|
| **Strategy + Factory** | `CancelStrategy` (`OwnerCancelStrategy`, `AdminCancelStrategy`) chọn bằng `CancelStrategyFactory` theo quan hệ người huỷ–chủ booking | Tránh `if (isAdmin) ... else ...` rải rác |
| **Chain of Responsibility / Strategy** | `BookingRule` interface (`validate(BookingContext)`), mỗi rule ở mục 6.1 là một class: `WorkingHoursRule`, `DurationRule`, `TimeSlotRule`, `LeadTimeRule`, `CapacityRule`, `UserOverlapRule`, `QuotaRule`... Service chạy danh sách rule được Spring inject (`List<BookingRule>`) theo `@Order` | Thêm/bớt rule không sửa `BookingService` (Open/Closed) |
| **Specification** | `RoomSpecification` cho lọc phòng (capacity, equipment, status, keyword) và `AvailabilityService` | Query động, không nối chuỗi SQL |
| **Observer** | Spring `ApplicationEventPublisher` + `@TransactionalEventListener(AFTER_COMMIT)` cho `BookingCreated/Rescheduled/Cancelled` | Tách việc thông báo/log khỏi luồng nghiệp vụ; sau này gắn email không cần sửa service |
| **Builder** | Lombok `@Builder` cho entity/DTO | |
| **Facade** | `BookingService` điều phối rule, repository, mapper, event | Controller chỉ gọi 1 điểm vào |

Không áp dụng pattern nào ngoài bảng trên nếu không có lý do rõ ràng.

### 7.4 DTO (theo AGENT.md mục 4) — không expose entity

Ví dụ tên DTO cần có (mỗi API một DTO riêng, chỉ chứa field cần thiết):

- Auth: `LoginRequest`, `LoginResponse`, `RefreshTokenRequest`, `ChangePasswordRequest`
- User: `CreateUserRequest`, `UpdateUserStatusRequest`, `UpdateUserRoleRequest`, `UserResponse`, `UserDetailResponse`, `UserListResponse`, `UserLookupRequest` (filter)
- Room: `CreateRoomRequest`, `UpdateRoomRequest`, `UpdateRoomStatusRequest`, `RoomResponse`, `RoomDetailResponse`, `RoomListResponse`, `RoomLookupRequest`, `AvailableRoomLookupRequest`
- Equipment: `CreateEquipmentRequest`, `UpdateEquipmentRequest`, `EquipmentResponse`
- Booking: `CreateBookingRequest`, `UpdateBookingRequest`, `CancelBookingRequest`, `BookingResponse`, `BookingDetailResponse`, `BookingListResponse`, `BookingLookupRequest`, `BookingHistoryResponse`
- Calendar: `RoomCalendarResponse`, `CalendarEntryResponse`, `CalendarLookupRequest`

### 7.5 Mapping & Lombok

- Dùng **MapStruct** cho mọi Entity ↔ DTO: `UserMapper`, `RoomMapper`, `EquipmentMapper`, `BookingMapper`. Cấu hình `componentModel = "spring"`; kết hợp Lombok cần thêm `lombok-mapstruct-binding` đúng thứ tự annotation processor trong build.
- `name_vi/name_en` ⇄ `LocalizedText { vi, en }` xử lý bằng MapStruct custom method dùng chung (một chỗ duy nhất, **tái sử dụng**, không viết lại ở từng mapper).
- Lombok: `@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`, inject bằng `@RequiredArgsConstructor` (không `@Autowired` trên field). Với entity JPA tránh `@Data`/`@EqualsAndHashCode` tự sinh trên quan hệ.

### 7.6 Transaction & concurrency

- `@Transactional` ở tầng Service; query chỉ đọc dùng `@Transactional(readOnly = true)`.
- Tạo booking: kiểm tra rule → `saveAndFlush` (flush để exclusion constraint nổ ngay trong transaction và được map thành 409).
- Isolation mặc định `READ COMMITTED` là đủ vì chốt chặn cuối là DB constraint.
- Huỷ hàng loạt khi đóng phòng (mục 6.4) nằm trong **một** transaction.

### 7.7 Bảo mật

- JWT access token ngắn (mặc định 30 phút), refresh token dài (mặc định 7 ngày); secret lấy từ biến môi trường, **không hardcode**, không commit giá trị thật. Khi khởi động, app **fail fast** nếu `JWT_SECRET` ngắn hơn 32 byte hoặc còn là placeholder trong `.env.example`, hoặc nếu `APP_ADMIN_PASSWORD` không đạt chính sách mật khẩu (mục 6.5) hay còn là placeholder; thông báo lỗi không in giá trị secret.
- Phân quyền bằng `@PreAuthorize("hasRole('ADMIN')")` / security config theo prefix `/admin/**`; kiểm tra quyền sở hữu booking ở **service** (không tin dữ liệu từ client).
- BCrypt cho mật khẩu; không log mật khẩu/token; không trả `password_hash`.
- CORS cấu hình qua env. Rate limit đăng nhập theo cơ chế khoá tạm ở mục 6.5.
- `must_change_password = true` → chỉ cho gọi `change-password` và `users/me` cho tới khi đổi xong (trả `403 PASSWORD_CHANGE_REQUIRED`).

### 7.8 Seed dữ liệu

Migration/seed runner tạo sẵn:

- 1 ADMIN mặc định (từ env).
- Danh mục Equipment mẫu: `PROJECTOR`, `VIDEO_CONF`, `WHITEBOARD`, `TV_SCREEN`, `SPEAKERPHONE`.
- 10 phòng mẫu (profile `docker`/`demo`, tắt được bằng `APP_SEED_DEMO_DATA=false`) với sức chứa 4–30 người để reviewer chạy là thấy dữ liệu ngay.

## 8. Docker & môi trường

Yêu cầu: sau khi tạo `.env` từ `.env.example` (một lệnh `cp`), **`docker compose up` chạy được toàn bộ hệ thống** (app + database), không cần cài thêm gì khác ngoài Docker.

- `Dockerfile` **multi-stage**: stage build (Maven/Gradle + JDK) → stage runtime (JRE slim), chạy bằng user không phải root.
- `docker-compose.yml`:
  - Service `db`: `postgres:16`, volume named, `healthcheck` bằng `pg_isready`.
  - Service `app`: `depends_on: db: condition: service_healthy`, expose `8080`, `healthcheck` qua `/actuator/health`.
  - Toàn bộ cấu hình qua biến môi trường. `.env.example` chỉ chứa **placeholder** (ví dụ `change-me`) cho mọi secret, không chứa giá trị dùng được; `.env` thật nằm trong `.gitignore`.
  - Với các biến secret (`JWT_SECRET`, `SPRING_DATASOURCE_PASSWORD` / `POSTGRES_PASSWORD`, `APP_ADMIN_PASSWORD`), `docker-compose.yml` dùng cú pháp bắt buộc `${VAR:?message}` — thiếu biến thì compose dừng ngay kèm thông báo rõ ràng. Biến không phải secret (cổng, `APP_TIMEZONE`, `BOOKING_*`, ...) được phép có giá trị mặc định.
- Flyway tự migrate khi app start; app retry kết nối DB hợp lý.
- Swagger UI: `http://localhost:8080/swagger-ui.html`.
- Có `.dockerignore` và `.gitignore`.

Biến môi trường tối thiểu: `SPRING_DATASOURCE_URL/USERNAME/PASSWORD`, `JWT_SECRET`, `JWT_ACCESS_TTL_MINUTES`, `JWT_REFRESH_TTL_DAYS`, `APP_ADMIN_EMAIL`, `APP_ADMIN_PASSWORD`, `APP_TIMEZONE`, `APP_SEED_DEMO_DATA`, `BOOKING_*` (các tham số ở mục 6.1).

> **[QUYẾT ĐỊNH]** Không commit và không đặt mặc định cho secret ở bất kỳ đâu (code, `application.yml`, `docker-compose.yml`, `.env.example`). Đổi lại, người chạy phải làm thêm một bước: `cp .env.example .env`, rồi điền secret (gợi ý sinh `JWT_SECRET` bằng `openssl rand -base64 48`). Lý do: tuân thủ AGENT.md (mục 0.4 và 10), tránh việc bản demo lên môi trường thật mà vẫn dùng secret công khai trong repo. Ghi rõ vào README.

## 9. Non-functional Requirements

- **Correctness:** Không bao giờ tồn tại 2 booking `CONFIRMED` giao nhau trên cùng phòng — và không bao giờ tồn tại 2 booking `CONFIRMED` giao nhau của cùng một user — cả hai được đảm bảo ở tầng DB (mục 4.7).
- **Latency:** `GET /calendar` và `GET /rooms/available` < 200ms p95 với quy mô ở mục 1.1 (nhờ index mục 4.8; kiểm tra bằng `EXPLAIN` khi cần, không cần cache).
- **Logging:**
  - Mọi request có `correlationId` / `requestId` (MDC).
  - Log sự kiện nghiệp vụ (tạo / đổi / huỷ booking) mức INFO.
  - Không log mật khẩu, token, dữ liệu nhạy cảm.
  - **Đóng gói log**: dùng Logback `TimeBasedRollingPolicy` theo ngày (`logs/app.yyyy-MM-dd.log`), giữ tối đa 14 ngày, max file size hợp lý.
  - Format log JSON (dễ parse sau này).
  - SQL query log: chỉ bật ở profile `dev` và `docker` (`logging.level.org.hibernate.SQL=DEBUG`). Production mặc định tắt.
  - Không gửi log ra hệ thống bên ngoài (ELK, Loki…) ở phiên bản này.
- **Health:** `/actuator/health` (liveness + readiness), chỉ mở các endpoint actuator cần thiết.
- **Audit:** `BookingHistory` lưu vĩnh viễn.
- **Compatibility:** API đặt dưới `/api/v1` để sau này nâng version không phá client cũ.
- **Timezone-safe:** mọi thời gian lưu/truyền là epoch ms UTC.

## 10. Quy tắc làm việc cho AI/Developer (từ AGENT.md — bắt buộc)

- **Không chạy lệnh build/test/run** sau khi code xong.
- **Không viết unit test.**
- Format code chuẩn (indent 4 spaces, import gọn, không import thừa).
- Trước khi tạo class/method mới: kiểm tra codebase xem đã có sẵn chưa; **tái sử dụng** (ví dụ `LocalizedText` mapping, `TimeUtils`, `PageResponse` dùng chung toàn dự án).
- Tuân thủ `Controller → Service → Repository`; DTO riêng cho từng API; MapStruct cho mapping; Lombok + constructor injection.
- Mọi API trả danh sách phải phân trang (`page=0`, `size=10`), dùng `Pageable`/`Page<T>`, không load cả bảng rồi cắt thủ công (ngoại lệ có lý do duy nhất: `calendar`, mục 5.4).
- Trường nội dung/tiêu đề do hệ thống quản lý phải localize `{vi, en}`; trường thời gian dùng `Long`.
- Không thêm dependency hoặc abstraction không cần thiết; mọi dependency ngoài danh sách mục 2 phải có lý do ghi trong README.

## 11. Việc cần làm khi triển khai (theo thứ tự)

1. Khởi tạo project (Maven/Gradle), dependency, cấu hình Lombok + MapStruct đúng thứ tự annotation processor.
2. Migration Flyway theo đúng thứ tự dependency: `Equipment`, `User` → `Room` → `RoomEquipment` → `Booking` (+ 2 exclusion constraint, index) → `BookingHistory`. Seed theo mục 7.8.
3. Tầng `common`: `ErrorCode`, `BusinessException`, `GlobalExceptionHandler` (i18n vi/en), `PageResponse`, `LocalizedText`, `TimeUtils`, `BaseEntity`.
4. Module `auth` + `user`: JWT, security config, API mục 5.1, seed admin, khoá tạm đăng nhập sai, kiểm tra secret khi khởi động (7.7).
5. Module `room` + `equipment`: API mục 5.2, Specification lọc phòng, rule mục 6.4.
6. Module `booking`: rule chain (6.1), tạo / đổi lịch / huỷ (6.2, 6.3) với Strategy + Factory, ghi `BookingHistory`, phát event, API xem lịch sử booking (5.3).
7. `CalendarService` và `AvailabilityService` (mục 5.4).
8. Cấu hình OpenAPI (mô tả mỗi endpoint, ví dụ request/response, các mã lỗi).
9. `Dockerfile`, `docker-compose.yml`, `.env.example`, `.dockerignore`.
10. Viết `README.md` (mục 12).
11. Rà soát cuối: đối chiếu từng mục file này + `AGENT.md`, tìm code trùng lặp, kiểm tra không có entity bị expose, không có secret hardcode.

## 12. Yêu cầu nội dung README.md

`README.md` ở thư mục gốc phải có đủ các phần sau:

1. **Giới thiệu** — bài toán và phạm vi, tính năng chính.
2. **Cách chạy** — yêu cầu (Docker), lệnh `docker compose up --build`, URL Swagger/health, bước tạo `.env` từ `.env.example` và cách sinh secret, tài khoản admin lấy từ `.env` (đổi mật khẩu ở lần đăng nhập đầu), cách dừng/xoá dữ liệu, cách đổi cấu hình qua env.
3. **Hướng dẫn dùng nhanh** — luồng mẫu bằng `curl`: login → xem phòng → tìm phòng trống → đặt phòng → xem lịch → huỷ.
4. **Kiến trúc & thiết kế** — sơ đồ tầng, cấu trúc package, ERD (Mermaid), luồng đặt phòng (sequence diagram), danh sách API (tóm tắt).
5. **Các quyết định quan trọng và lý do** — tối thiểu gồm:
   - Monolith + PostgreSQL, không Redis/Kafka (vì quy mô 10 phòng/200 user).
   - Exclusion constraint chống trùng lịch ở tầng DB thay vì lock ứng dụng (cho cả phòng và cho từng user); quota chỉ kiểm tra ở service nên có thể vượt vài booking khi đồng thời.
   - Không có secret mặc định trong repo: `.env.example` chỉ có placeholder, app fail fast nếu secret yếu hoặc còn là placeholder; đổi lại cần một bước `cp .env.example .env` khi chạy.
   - Khoảng thời gian nửa mở `[start, end)`.
   - Thời gian dạng epoch ms UTC (`Long`) + múi giờ nghiệp vụ cấu hình được.
   - JWT stateless; không có self-signup; admin tạo user.
   - Huỷ bằng `POST /cancel` + audit thay vì `DELETE`.
   - Rule engine dạng danh sách `BookingRule` cấu hình được.
   - Localize `{vi, en}` cho nội dung hệ thống; `title` booking không localize.
   - Calendar không phân trang nhưng giới hạn 31 ngày.
   - Mỗi thư viện thêm ngoài danh sách chuẩn.
   - Logging: Logback rolling theo ngày (14 ngày) + JSON + correlationId; SQL log chỉ bật ở profile dev/docker. Lý do: đủ để debug và đóng gói log hàng ngày với quy mô nhỏ, tránh over-engineering.
6. **Giả định** — các điểm đề bài chưa nói rõ và cách đã chọn (giờ làm việc, thời lượng, quota, minh bạch lịch họp...).
7. **Giới hạn đã biết & hướng mở rộng** — recurring booking, thông báo email, SSO, lịch private, cache/read replica khi tăng quy mô, tự huỷ booking khi khoá user.
8. **Ghi chú về kiểm thử** — theo quy ước dự án không viết unit test; mô tả cách tự kiểm thử thủ công qua Swagger/curl, đặc biệt kịch bản **2 request đặt cùng phòng cùng lúc**.

## 13. Tiêu chí hoàn thành (Definition of Done)

- [ ] Sau khi `cp .env.example .env` và điền secret, `docker compose up` khởi động được toàn bộ (app + DB), migration và seed chạy tự động; thiếu secret thì dừng với thông báo rõ ràng.
- [ ] Swagger UI hiển thị đầy đủ API với mô tả và ví dụ.
- [ ] Hai request đặt trùng phòng/khung giờ đồng thời → đúng 1 thành công, 1 nhận `409 ROOM_ALREADY_BOOKED`.
- [ ] Cùng một user đặt đồng thời 2 phòng khác nhau trùng khung giờ → đúng 1 thành công, 1 nhận `409 USER_DOUBLE_BOOKED`.
- [ ] Chủ booking và ADMIN xem được lịch sử booking (phân trang); user khác nhận `403`.
- [ ] EMPLOYEE không thể sửa/huỷ booking của người khác; không truy cập được `/admin/**`.
- [ ] Mọi API danh sách có phân trang mặc định `page=0,size=10` (trừ calendar có giới hạn 31 ngày).
- [ ] Không có entity JPA nào bị trả trực tiếp qua API; mapping bằng MapStruct.
- [ ] Mọi trường thời gian là `Long`; nội dung hệ thống localize `{vi, en}`; lỗi trả theo `Accept-Language`.
- [ ] Không có secret hardcode hay mặc định; `.env.example` chỉ chứa placeholder; app từ chối khởi động nếu `JWT_SECRET` / `APP_ADMIN_PASSWORD` yếu hoặc còn là placeholder.
- [ ] README đủ 8 phần ở mục 12.