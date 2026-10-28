package com.company.roombooking.booking.rule;

import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(40)
public class DurationRule implements BookingRule {

    @Override
    public void validate(BookingContext context) throws BusinessException {
        long durationMillis = context.getEndTime() - context.getStartTime();
        long durationMinutes = durationMillis / 60000L;

        int minMinutes = context.getProperties().getMinDurationMinutes();
        int maxMinutes = context.getProperties().getMaxDurationMinutes();

        if (durationMinutes < minMinutes || durationMinutes > maxMinutes) {
            throw new BusinessException(ErrorCode.INVALID_DURATION);
        }
    }
}
