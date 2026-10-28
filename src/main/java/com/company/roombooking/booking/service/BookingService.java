package com.company.roombooking.booking.service;

import com.company.roombooking.booking.cancel.CancelStrategy;
import com.company.roombooking.booking.cancel.CancelStrategyFactory;
import com.company.roombooking.booking.dto.BookingResponse;
import com.company.roombooking.booking.dto.CancelBookingRequest;
import com.company.roombooking.booking.dto.CreateBookingRequest;
import com.company.roombooking.booking.dto.UpdateBookingRequest;
import com.company.roombooking.booking.entity.Booking;
import com.company.roombooking.booking.entity.BookingAction;
import com.company.roombooking.booking.entity.BookingHistory;
import com.company.roombooking.booking.entity.BookingStatus;
import com.company.roombooking.booking.event.BookingCancelledEvent;
import com.company.roombooking.booking.event.BookingCreatedEvent;
import com.company.roombooking.booking.event.BookingRescheduledEvent;
import com.company.roombooking.booking.mapper.BookingMapper;
import com.company.roombooking.booking.repository.BookingHistoryRepository;
import com.company.roombooking.booking.repository.BookingRepository;
import com.company.roombooking.booking.rule.BookingContext;
import com.company.roombooking.booking.rule.BookingRule;
import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ConstraintViolationTranslator;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.common.util.SecurityUtils;
import com.company.roombooking.config.BookingProperties;
import com.company.roombooking.room.entity.Room;
import com.company.roombooking.room.repository.RoomRepository;
import com.company.roombooking.user.entity.User;
import com.company.roombooking.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;
    private final List<BookingRule> bookingRules;
    private final CancelStrategyFactory cancelStrategyFactory;
    private final ConstraintViolationTranslator constraintViolationTranslator;
    private final ApplicationEventPublisher eventPublisher;
    private final BookingProperties bookingProperties;

    @Value("${app.timezone:Asia/Ho_Chi_Minh}")
    private String appTimezone;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        long now = System.currentTimeMillis();
        ZoneId zoneId = ZoneId.of(appTimezone);

        BookingContext context = BookingContext.builder()
                .room(room)
                .user(user)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .attendeeCount(request.getAttendeeCount())
                .existingBooking(null)
                .currentTime(now)
                .zoneId(zoneId)
                .properties(bookingProperties)
                .build();

        // 1. Run all domain business rules
        for (BookingRule rule : bookingRules) {
            rule.validate(context);
        }

        Booking booking = Booking.builder()
                .room(room)
                .bookedBy(user)
                .title(request.getTitle().trim())
                .description(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null)
                .attendeeCount(request.getAttendeeCount())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .status(BookingStatus.CONFIRMED)
                .version(0L)
                .build();

        // 2. Save & Flush to trigger DB exclusion constraints immediately
        Booking savedBooking;
        try {
            savedBooking = bookingRepository.saveAndFlush(booking);
        } catch (DataIntegrityViolationException ex) {
            throw constraintViolationTranslator.translate(ex);
        }

        // 3. Record Audit History
        BookingHistory history = BookingHistory.builder()
                .booking(savedBooking)
                .action(BookingAction.CREATED)
                .oldStartTime(null)
                .oldEndTime(null)
                .newStartTime(savedBooking.getStartTime())
                .newEndTime(savedBooking.getEndTime())
                .actor(user)
                .note("Đặt phòng mới thành công")
                .createdAt(now)
                .build();
        bookingHistoryRepository.save(history);

        // 4. Publish Event
        eventPublisher.publishEvent(new BookingCreatedEvent(
                savedBooking.getBookingId(),
                room.getRoomId(),
                user.getUserId(),
                savedBooking.getTitle(),
                savedBooking.getStartTime(),
                savedBooking.getEndTime()
        ));

        return bookingMapper.toResponse(savedBooking);
    }

    @Transactional
    public BookingResponse updateBooking(Long bookingId, UpdateBookingRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean isOwner = booking.getBookedBy().getUserId().equals(currentUserId);
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();
        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Bạn không có quyền chỉnh sửa lịch đặt phòng này");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_MODIFIABLE, "Chỉ có thể sửa lịch họp đang ở trạng thái CONFIRMED");
        }

        long now = System.currentTimeMillis();
        if (now >= booking.getStartTime()) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_MODIFIABLE, "Không thể sửa lịch họp đã bắt đầu hoặc đã qua");
        }

        ZoneId zoneId = ZoneId.of(appTimezone);

        BookingContext context = BookingContext.builder()
                .room(booking.getRoom())
                .user(booking.getBookedBy())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .attendeeCount(request.getAttendeeCount())
                .existingBooking(booking)
                .currentTime(now)
                .zoneId(zoneId)
                .properties(bookingProperties)
                .build();

        // 1. Re-validate all domain rules
        for (BookingRule rule : bookingRules) {
            rule.validate(context);
        }

        long oldStartTime = booking.getStartTime();
        long oldEndTime = booking.getEndTime();

        booking.setTitle(request.getTitle().trim());
        booking.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
        booking.setAttendeeCount(request.getAttendeeCount());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());

        // 2. Save & Flush with optimistic locking check and exclusion constraints
        Booking updatedBooking;
        try {
            updatedBooking = bookingRepository.saveAndFlush(booking);
        } catch (DataIntegrityViolationException ex) {
            throw constraintViolationTranslator.translate(ex);
        }

        // 3. Record Audit History
        BookingHistory history = BookingHistory.builder()
                .booking(updatedBooking)
                .action(BookingAction.RESCHEDULED)
                .oldStartTime(oldStartTime)
                .oldEndTime(oldEndTime)
                .newStartTime(updatedBooking.getStartTime())
                .newEndTime(updatedBooking.getEndTime())
                .actor(currentUser)
                .note("Đổi lịch / thông tin cuộc họp")
                .createdAt(now)
                .build();
        bookingHistoryRepository.save(history);

        // 4. Publish Event
        eventPublisher.publishEvent(new BookingRescheduledEvent(
                updatedBooking.getBookingId(),
                updatedBooking.getRoom().getRoomId(),
                currentUser.getUserId(),
                oldStartTime,
                oldEndTime,
                updatedBooking.getStartTime(),
                updatedBooking.getEndTime()
        ));

        return bookingMapper.toResponse(updatedBooking);
    }

    @Transactional
    public void cancelBooking(Long bookingId, CancelBookingRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // Use Strategy + Factory to validate and apply cancellation according to role & ownership
        CancelStrategy strategy = cancelStrategyFactory.getStrategy(booking, currentUser);
        strategy.validateAndCancel(booking, currentUser, request);

        bookingRepository.saveAndFlush(booking);

        long now = System.currentTimeMillis();

        // Record Audit History
        BookingHistory history = BookingHistory.builder()
                .booking(booking)
                .action(BookingAction.CANCELLED)
                .oldStartTime(booking.getStartTime())
                .oldEndTime(booking.getEndTime())
                .newStartTime(null)
                .newEndTime(null)
                .actor(currentUser)
                .note(booking.getCancelReason())
                .createdAt(now)
                .build();
        bookingHistoryRepository.save(history);

        // Publish Event
        eventPublisher.publishEvent(new BookingCancelledEvent(
                booking.getBookingId(),
                booking.getRoom().getRoomId(),
                currentUser.getUserId(),
                booking.getBookedBy().getUserId(),
                booking.getCancelReason()
        ));
    }
}
