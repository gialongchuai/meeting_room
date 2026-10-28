package com.company.roombooking.booking.repository;

import com.company.roombooking.booking.entity.BookingHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingHistoryRepository extends JpaRepository<BookingHistory, Long> {

    @Query(value = """
        SELECT bh FROM BookingHistory bh
        JOIN FETCH bh.actor
        WHERE bh.booking.bookingId = :bookingId
    """, countQuery = """
        SELECT COUNT(bh) FROM BookingHistory bh
        WHERE bh.booking.bookingId = :bookingId
    """)
    Page<BookingHistory> findByBookingIdWithActor(@Param("bookingId") Long bookingId, Pageable pageable);
}
