package com.company.roombooking.booking.rule;

import com.company.roombooking.booking.repository.BookingRepository;
import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(120)
public class RoomOverlapRule implements BookingRule {

    private final BookingRepository bookingRepository;

    public RoomOverlapRule(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void validate(BookingContext context) throws BusinessException {
        Long excludeBookingId = context.getExistingBooking() != null
                ? context.getExistingBooking().getBookingId()
                : null;

        boolean overlap = bookingRepository.existsOverlappingRoomBooking(
                context.getRoom().getRoomId(),
                context.getStartTime(),
                context.getEndTime(),
                excludeBookingId
        );

        if (overlap) {
            throw new BusinessException(ErrorCode.ROOM_ALREADY_BOOKED);
        }
    }
}
