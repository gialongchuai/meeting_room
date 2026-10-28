package com.company.roombooking.booking.rule;

import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.common.util.TimeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class SingleDayRule implements BookingRule {

    @Override
    public void validate(BookingContext context) throws BusinessException {
        // [start, end) interval: end - 1ms falls on the same calendar day as start
        if (!TimeUtils.isSameCalendarDay(context.getStartTime(), context.getEndTime() - 1, context.getZoneId())) {
            throw new BusinessException(ErrorCode.MULTI_DAY_NOT_ALLOWED);
        }
    }
}
