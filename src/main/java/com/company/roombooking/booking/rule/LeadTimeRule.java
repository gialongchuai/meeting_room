package com.company.roombooking.booking.rule;

import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(70)
public class LeadTimeRule implements BookingRule {

    @Override
    public void validate(BookingContext context) throws BusinessException {
        long maxLeadMillis = context.getProperties().getMaxLeadDays() * 24L * 60 * 60 * 1000L;
        if (context.getStartTime() > context.getCurrentTime() + maxLeadMillis) {
            throw new BusinessException(ErrorCode.BOOKING_TOO_FAR_AHEAD);
        }
    }
}
