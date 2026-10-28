package com.company.roombooking.booking.cancel;

import com.company.roombooking.booking.entity.Booking;
import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.user.entity.User;
import com.company.roombooking.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CancelStrategyFactory {

    private final OwnerCancelStrategy ownerCancelStrategy;
    private final AdminCancelStrategy adminCancelStrategy;

    public CancelStrategy getStrategy(Booking booking, User currentUser) {
        if (currentUser.getUserId().equals(booking.getBookedBy().getUserId())) {
            return ownerCancelStrategy;
        }
        if (currentUser.getRole() == UserRole.ADMIN) {
            return adminCancelStrategy;
        }
        throw new BusinessException(ErrorCode.FORBIDDEN, "Bạn không có quyền hủy lịch đặt phòng này");
    }
}
