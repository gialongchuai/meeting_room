package com.company.roombooking.booking.rule;

import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(60)
public class PastTimeRule implements BookingRule {

    @Override
    public void validate(BookingContext context) throws BusinessException {
        if (context.getStartTime() < context.getCurrentTime()) {
            throw new BusinessException(ErrorCode.BOOKING_IN_THE_PAST);
        }
    }
}
