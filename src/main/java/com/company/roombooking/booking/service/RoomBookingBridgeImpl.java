package com.company.roombooking.booking.service;

import com.company.roombooking.booking.entity.Booking;
import com.company.roombooking.booking.entity.BookingAction;
import com.company.roombooking.booking.entity.BookingHistory;
import com.company.roombooking.booking.entity.BookingStatus;
import com.company.roombooking.booking.event.BookingCancelledEvent;
import com.company.roombooking.booking.repository.BookingHistoryRepository;
import com.company.roombooking.booking.repository.BookingRepository;
import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.room.service.RoomBookingBridge;
import com.company.roombooking.user.entity.User;
import com.company.roombooking.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoomBookingBridgeImpl implements RoomBookingBridge {

    private final BookingRepository bookingRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public long countFutureConfirmedBookings(Long roomId, long now) {
        return bookingRepository.countFutureConfirmedBookingsByRoom(roomId, now);
    }

    @Override
    public boolean existsFutureBookingWithMoreAttendees(Long roomId, int capacity, long now) {
        return bookingRepository.existsFutureBookingWithMoreAttendees(roomId, capacity, now);
    }

    @Override
    public void cancelAllFutureBookingsForRoom(Long roomId, String reason, Long cancelledByUserId) {
        long now = System.currentTimeMillis();
        User admin = userRepository.findById(cancelledByUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Booking> futureBookings = bookingRepository.findFutureConfirmedBookingsByRoom(roomId, now);
        for (Booking booking : futureBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelledBy(admin);
            booking.setCancelledAt(now);
            booking.setCancelReason(reason);
            bookingRepository.save(booking);

            BookingHistory history = BookingHistory.builder()
                    .booking(booking)
                    .action(BookingAction.CANCELLED)
                    .oldStartTime(booking.getStartTime())
                    .oldEndTime(booking.getEndTime())
                    .newStartTime(null)
                    .newEndTime(null)
                    .actor(admin)
                    .note(reason)
                    .createdAt(now)
                    .build();
            bookingHistoryRepository.save(history);

            eventPublisher.publishEvent(new BookingCancelledEvent(
                    booking.getBookingId(),
                    roomId,
                    admin.getUserId(),
                    booking.getBookedBy().getUserId(),
                    reason
            ));
        }

        log.info("Đã tự động hủy {} lịch đặt phòng tương lai của phòng ID={} do đóng phòng/bảo trì",
                futureBookings.size(), roomId);
    }
}
