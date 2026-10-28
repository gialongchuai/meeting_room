package com.company.roombooking.booking.event;

public record BookingCreatedEvent(
        Long bookingId,
        Long roomId,
        Long bookedByUserId,
        String title,
        Long startTime,
        Long endTime
) {
}
