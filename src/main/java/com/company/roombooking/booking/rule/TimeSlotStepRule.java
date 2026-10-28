package com.company.roombooking.booking.rule;

import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.common.util.TimeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(50)
public class TimeSlotStepRule implements BookingRule {

    @Override
    public void validate(BookingContext context) throws BusinessException {
        int step = context.getProperties().getTimeSlotStepMinutes();
        if (!TimeUtils.isStepMultiple(context.getStartTime(), step)
                || !TimeUtils.isStepMultiple(context.getEndTime(), step)) {
            throw new BusinessException(ErrorCode.INVALID_TIME_SLOT);
        }
    }
}
