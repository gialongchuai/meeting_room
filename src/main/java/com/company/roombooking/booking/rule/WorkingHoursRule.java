package com.company.roombooking.booking.rule;

import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.common.util.TimeUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(80)
public class WorkingHoursRule implements BookingRule {

    @Override
    public void validate(BookingContext context) throws BusinessException {
        boolean inHours = TimeUtils.isInWorkingHours(
                context.getStartTime(),
                context.getEndTime(),
                context.getZoneId(),
                context.getProperties().getParsedWorkingHoursStart(),
                context.getProperties().getParsedWorkingHoursEnd()
        );
        if (!inHours) {
            throw new BusinessException(ErrorCode.OUTSIDE_WORKING_HOURS);
        }
    }
}
