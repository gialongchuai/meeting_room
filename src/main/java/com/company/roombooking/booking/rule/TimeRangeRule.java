package com.company.roombooking.booking.rule;

import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(30)
public class TimeRangeRule implements BookingRule {

    @Override
    public void validate(BookingContext context) throws BusinessException {
        if (context.getEndTime() == null || context.getStartTime() == null
                || context.getEndTime() <= context.getStartTime()) {
            throw new BusinessException(ErrorCode.INVALID_TIME_RANGE);
        }
    }
}
