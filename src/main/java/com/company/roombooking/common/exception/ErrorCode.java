package com.company.roombooking.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 400 Bad Request
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ"),
    CALENDAR_RANGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "Khoảng thời gian xem lịch vượt quá tối đa 31 ngày"),
    INVALID_DURATION(HttpStatus.BAD_REQUEST, "Thời lượng họp phải từ 15 phút đến 4 giờ"),
    INVALID_TIME_SLOT(HttpStatus.BAD_REQUEST, "Thời gian bắt đầu và kết thúc phải là bội số của 15 phút"),
    BOOKING_IN_THE_PAST(HttpStatus.BAD_REQUEST, "Không thể đặt phòng trong quá khứ"),
    BOOKING_TOO_FAR_AHEAD(HttpStatus.BAD_REQUEST, "Chỉ được phép đặt phòng trước tối đa 60 ngày"),
    OUTSIDE_WORKING_HOURS(HttpStatus.BAD_REQUEST, "Thời gian đặt phòng phải nằm trong giờ làm việc (08:00 - 18:00)"),
    OUTSIDE_WORKING_DAYS(HttpStatus.BAD_REQUEST, "Chỉ được đặt phòng từ Thứ 2 đến Thứ 6"),
    MULTI_DAY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "Cuộc họp không được kéo dài qua nhiều ngày"),
    ATTENDEE_EXCEEDS_CAPACITY(HttpStatus.BAD_REQUEST, "Số người tham gia vượt quá sức chứa của phòng"),
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "Thời gian kết thúc phải lớn hơn thời gian bắt đầu"),
    BOOKING_QUOTA_EXCEEDED(HttpStatus.BAD_REQUEST, "Đã đạt giới hạn tối đa 20 cuộc họp đang hoạt động"),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "Kích thước trang không hợp lệ (tối đa 100)"),
    INVALID_SORT_FIELD(HttpStatus.BAD_REQUEST, "Trường sắp xếp không được phép"),

    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Chưa đăng nhập hoặc phiên làm việc đã hết hạn"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không chính xác"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token xác thực đã hết hạn"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Token xác thực không hợp lệ"),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "Không có quyền thực hiện thao tác này"),
    PASSWORD_CHANGE_REQUIRED(HttpStatus.FORBIDDEN, "Bắt buộc phải đổi mật khẩu trước khi tiếp tục"),
    USER_LOCKED(HttpStatus.FORBIDDEN, "Tài khoản của bạn đã bị khóa"),

    // 404 Not Found
    NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy tài nguyên"),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy phòng họp"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"),
    BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy lịch đặt phòng"),
    EQUIPMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy tiện ích thiết bị"),

    // 409 Conflict
    ROOM_ALREADY_BOOKED(HttpStatus.CONFLICT, "Phòng đã được đặt trong khung giờ này"),
    USER_DOUBLE_BOOKED(HttpStatus.CONFLICT, "Bạn đã có lịch họp khác trùng vào khung giờ này"),
    ROOM_NOT_AVAILABLE(HttpStatus.CONFLICT, "Phòng hiện không ở trạng thái sẵn sàng sử dụng"),
    ROOM_HAS_FUTURE_BOOKINGS(HttpStatus.CONFLICT, "Phòng còn các lịch đặt trong tương lai"),
    CAPACITY_CONFLICTS_WITH_BOOKINGS(HttpStatus.CONFLICT, "Sức chứa mới nhỏ hơn số người tham gia của các lịch họp tương lai"),
    CANNOT_MODIFY_SELF(HttpStatus.CONFLICT, "Không thể tự khóa tài khoản hoặc tự hạ quyền của chính mình"),
    BOOKING_NOT_MODIFIABLE(HttpStatus.CONFLICT, "Chỉ có thể sửa lịch họp đang ở trạng thái CONFIRMED và chưa bắt đầu"),
    BOOKING_ALREADY_CANCELLED(HttpStatus.CONFLICT, "Lịch đặt phòng này đã bị hủy trước đó"),
    BOOKING_CONCURRENT_MODIFICATION(HttpStatus.CONFLICT, "Lịch đặt phòng đã bị sửa đổi bởi người khác, vui lòng thử lại"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email này đã được sử dụng"),
    ROOM_CODE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Mã phòng họp đã tồn tại"),
    EQUIPMENT_CODE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Mã thiết bị đã tồn tại"),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống, vui lòng thử lại sau");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
