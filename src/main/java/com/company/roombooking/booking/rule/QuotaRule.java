package com.company.roombooking.booking.rule;

import com.company.roombooking.booking.repository.BookingRepository;
import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(130)
public class QuotaRule implements BookingRule {

    private final BookingRepository bookingRepository;

    public QuotaRule(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void validate(BookingContext context) throws BusinessException {
        // Admin is exempt from quota
        if (context.getUser().getRole() == UserRole.ADMIN) {
            return;
        }

        // Only enforce quota when creating a new booking
        if (context.getExistingBooking() == null) {
            long activeBookings = bookingRepository.countActiveFutureBookingsByUser(
                    context.getUser().getUserId(),
                    context.getCurrentTime()
            );

            if (activeBookings >= context.getProperties().getQuotaPerUser()) {
                throw new BusinessException(ErrorCode.BOOKING_QUOTA_EXCEEDED);
            }
        }
    }
}
