package com.company.roombooking.booking.rule;

import com.company.roombooking.booking.entity.Booking;
import com.company.roombooking.config.BookingProperties;
import com.company.roombooking.room.entity.Room;
import com.company.roombooking.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.ZoneId;

@Getter
@Builder
public class BookingContext {

    private final Room room;
    private final User user;
    private final Long startTime;
    private final Long endTime;
    private final int attendeeCount;
    private final Booking existingBooking;
    private final Long currentTime;
    private final ZoneId zoneId;
    private final BookingProperties properties;
}
