package com.company.roombooking.booking.rule;

import com.company.roombooking.booking.repository.BookingRepository;
import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(110)
@RequiredArgsConstructor
public class UserOverlapRule implements BookingRule {

    private final BookingRepository bookingRepository;

    @Override
    public void validate(BookingContext context) throws BusinessException {
        Long excludeBookingId = context.getExistingBooking() != null
                ? context.getExistingBooking().getBookingId()
                : null;

        boolean overlap = bookingRepository.existsOverlappingUserBooking(
                context.getUser().getUserId(),
                context.getStartTime(),
                context.getEndTime(),
                excludeBookingId
        );

        if (overlap) {
            throw new BusinessException(ErrorCode.USER_DOUBLE_BOOKED);
        }
    }
}
