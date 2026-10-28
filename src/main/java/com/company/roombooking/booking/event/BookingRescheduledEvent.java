package com.company.roombooking.booking.event;

public record BookingRescheduledEvent(
        Long bookingId,
        Long roomId,
        Long actorUserId,
        Long oldStartTime,
        Long oldEndTime,
        Long newStartTime,
        Long newEndTime
) {
}
