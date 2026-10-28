package com.company.roombooking.booking.repository;

import com.company.roombooking.booking.entity.Booking;
import com.company.roombooking.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.room.roomId = :roomId
          AND b.status = 'CONFIRMED'
          AND b.startTime < :endTime
          AND b.endTime > :startTime
          AND (:excludeBookingId IS NULL OR b.bookingId != :excludeBookingId)
    """)
    boolean existsOverlappingRoomBooking(
            @Param("roomId") Long roomId,
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime,
            @Param("excludeBookingId") Long excludeBookingId
    );

    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.bookedBy.userId = :userId
          AND b.status = 'CONFIRMED'
          AND b.startTime < :endTime
          AND b.endTime > :startTime
          AND (:excludeBookingId IS NULL OR b.bookingId != :excludeBookingId)
    """)
    boolean existsOverlappingUserBooking(
            @Param("userId") Long userId,
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime,
            @Param("excludeBookingId") Long excludeBookingId
    );

    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.bookedBy.userId = :userId
          AND b.status = 'CONFIRMED'
          AND b.endTime > :now
    """)
    long countActiveFutureBookingsByUser(@Param("userId") Long userId, @Param("now") Long now);

    @Query("""
        SELECT COUNT(b) FROM Booking b
        WHERE b.room.roomId = :roomId
          AND b.status = 'CONFIRMED'
          AND b.endTime > :now
    """)
    long countFutureConfirmedBookingsByRoom(@Param("roomId") Long roomId, @Param("now") Long now);

    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.room
        JOIN FETCH b.bookedBy
        WHERE b.room.roomId = :roomId
          AND b.status = 'CONFIRMED'
          AND b.endTime > :now
        ORDER BY b.startTime ASC
    """)
    List<Booking> findFutureConfirmedBookingsByRoom(@Param("roomId") Long roomId, @Param("now") Long now);

    @Query("""
        SELECT COUNT(b) > 0 FROM Booking b
        WHERE b.room.roomId = :roomId
          AND b.status = 'CONFIRMED'
          AND b.endTime > :now
          AND b.attendeeCount > :capacity
    """)
    boolean existsFutureBookingWithMoreAttendees(
            @Param("roomId") Long roomId,
            @Param("capacity") int capacity,
            @Param("now") Long now
    );

    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.bookedBy
        JOIN FETCH b.room
        WHERE b.room.roomId = :roomId
          AND b.status = 'CONFIRMED'
          AND b.startTime < :toTime
          AND b.endTime > :fromTime
        ORDER BY b.startTime ASC
    """)
    List<Booking> findRoomCalendar(
            @Param("roomId") Long roomId,
            @Param("fromTime") Long fromTime,
            @Param("toTime") Long toTime
    );

    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.bookedBy
        JOIN FETCH b.room
        WHERE b.status = 'CONFIRMED'
          AND b.startTime < :toTime
          AND b.endTime > :fromTime
        ORDER BY b.room.roomId ASC, b.startTime ASC
    """)
    List<Booking> findCompanyCalendar(
            @Param("fromTime") Long fromTime,
            @Param("toTime") Long toTime
    );
}
