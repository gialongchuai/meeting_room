package com.company.roombooking.booking.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class BookingEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingCreated(BookingCreatedEvent event) {
        log.info("SỰ KIỆN: Đã tạo lịch đặt phòng thành công - bookingId={}, roomId={}, userId={}, startTime={}, endTime={}",
                event.bookingId(), event.roomId(), event.bookedByUserId(), event.startTime(), event.endTime());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingRescheduled(BookingRescheduledEvent event) {
        log.info("SỰ KIỆN: Đã đổi lịch đặt phòng thành công - bookingId={}, roomId={}, actorId={}, oldTime=[{}-{}], newTime=[{}-{}]",
                event.bookingId(), event.roomId(), event.actorUserId(),
                event.oldStartTime(), event.oldEndTime(), event.newStartTime(), event.newEndTime());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingCancelled(BookingCancelledEvent event) {
        log.info("SỰ KIỆN: Đã hủy lịch đặt phòng thành công - bookingId={}, roomId={}, cancelledBy={}, bookedBy={}, reason='{}'",
                event.bookingId(), event.roomId(), event.cancelledByUserId(), event.bookedByUserId(), event.reason());
    }
}
