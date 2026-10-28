package com.company.roombooking.booking.rule;

import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(20)
public class CapacityRule implements BookingRule {

    @Override
    public void validate(BookingContext context) throws BusinessException {
        if (context.getAttendeeCount() > context.getRoom().getCapacity()) {
            throw new BusinessException(ErrorCode.ATTENDEE_EXCEEDS_CAPACITY);
        }
    }
}
