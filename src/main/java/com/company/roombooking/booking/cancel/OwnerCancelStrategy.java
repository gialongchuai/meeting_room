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
public class OwnerCancelStrategy implements CancelStrategy {

    @Override
    public void validateAndCancel(Booking booking, User currentUser, CancelBookingRequest request) {
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_MODIFIABLE);
        }

        long now = System.currentTimeMillis();
        if (now >= booking.getStartTime()) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_MODIFIABLE,
                    "Không thể hủy cuộc họp đã bắt đầu hoặc đã qua");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledBy(currentUser);
        booking.setCancelledAt(now);
        if (request != null && StringUtils.hasText(request.getReason())) {
            booking.setCancelReason(request.getReason().trim());
        } else {
            booking.setCancelReason("Người đặt tự hủy");
        }
    }
}
