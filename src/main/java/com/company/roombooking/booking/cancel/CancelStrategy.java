package com.company.roombooking.booking.cancel;

import com.company.roombooking.booking.dto.CancelBookingRequest;
import com.company.roombooking.booking.entity.Booking;
import com.company.roombooking.user.entity.User;

public interface CancelStrategy {

    void validateAndCancel(Booking booking, User currentUser, CancelBookingRequest request);
}
