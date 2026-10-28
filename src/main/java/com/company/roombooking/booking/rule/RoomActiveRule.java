package com.company.roombooking.booking.rule;

import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.room.entity.RoomStatus;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(10)
public class RoomActiveRule implements BookingRule {

    @Override
    public void validate(BookingContext context) throws BusinessException {
        if (context.getRoom().getStatus() != RoomStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.ROOM_NOT_AVAILABLE);
        }
    }
}
