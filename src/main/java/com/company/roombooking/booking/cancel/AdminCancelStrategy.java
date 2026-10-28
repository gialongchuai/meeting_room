package com.company.roombooking.booking.cancel;

import com.company.roombooking.booking.dto.CancelBookingRequest;
import com.company.roombooking.booking.entity.Booking;
import com.company.roombooking.booking.entity.BookingStatus;
import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.user.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AdminCancelStrategy implements CancelStrategy {

    @Override
    public void validateAndCancel(Booking booking, User currentUser, CancelBookingRequest request) {
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_MODIFIABLE);
        }

        long now = System.currentTimeMillis();
        if (now >= booking.getEndTime()) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_MODIFIABLE,
                    "Không thể hủy cuộc họp đã kết thúc");
        }

        if (request == null || !StringUtils.hasText(request.getReason())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "Lý do hủy là bắt buộc khi Quản trị viên hủy lịch họp của người khác");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledBy(currentUser);
        booking.setCancelledAt(now);
        booking.setCancelReason(request.getReason().trim());
    }
}
