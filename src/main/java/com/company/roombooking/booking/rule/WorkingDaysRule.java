package com.company.roombooking.booking.rule;

import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.common.util.TimeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(90)
public class WorkingDaysRule implements BookingRule {

    @Override
    public void validate(BookingContext context) throws BusinessException {
        // startTime and endTime - 1ms must be within working days (Monday - Friday)
        if (!TimeUtils.isWorkingDay(context.getStartTime(), context.getZoneId())
                || !TimeUtils.isWorkingDay(context.getEndTime() - 1, context.getZoneId())) {
            throw new BusinessException(ErrorCode.OUTSIDE_WORKING_DAYS);
        }
    }
}
