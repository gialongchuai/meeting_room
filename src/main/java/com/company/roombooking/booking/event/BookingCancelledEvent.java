package com.company.roombooking.booking.event;

public record BookingCancelledEvent(
        Long bookingId,
        Long roomId,
        Long cancelledByUserId,
        Long bookedByUserId,
        String reason
) {
}
