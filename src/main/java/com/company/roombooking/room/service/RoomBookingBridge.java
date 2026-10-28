package com.company.roombooking.room.service;

public interface RoomBookingBridge {

    long countFutureConfirmedBookings(Long roomId, long now);

    boolean existsFutureBookingWithMoreAttendees(Long roomId, int capacity, long now);

    void cancelAllFutureBookingsForRoom(Long roomId, String reason, Long cancelledByUserId);
}
