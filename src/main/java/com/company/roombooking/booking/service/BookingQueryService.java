package com.company.roombooking.booking.service;

import com.company.roombooking.booking.dto.BookingDetailResponse;
import com.company.roombooking.booking.dto.BookingHistoryResponse;
import com.company.roombooking.booking.dto.BookingLookupRequest;
import com.company.roombooking.booking.dto.BookingResponse;
import com.company.roombooking.booking.entity.Booking;
import com.company.roombooking.booking.entity.BookingHistory;
import com.company.roombooking.booking.mapper.BookingMapper;
import com.company.roombooking.booking.repository.BookingHistoryRepository;
import com.company.roombooking.booking.repository.BookingRepository;
import com.company.roombooking.common.dto.PageResponse;
import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.common.util.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookingQueryService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("startTime", "createdAt");

    private final BookingRepository bookingRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final BookingMapper bookingMapper;

    @Transactional(readOnly = true)
    public BookingDetailResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isOwner = booking.getBookedBy().getUserId().equals(currentUserId);
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();

        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Bạn không có quyền xem thông tin lịch đặt phòng này");
        }

        return bookingMapper.toDetailResponse(booking);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> getMyBookings(BookingLookupRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        request.setUserId(currentUserId);
        return queryBookings(request);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingResponse> getAllBookings(BookingLookupRequest request) {
        if (!SecurityUtils.isCurrentUserAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return queryBookings(request);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookingHistoryResponse> getBookingHistory(Long bookingId, int page, int size) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isOwner = booking.getBookedBy().getUserId().equals(currentUserId);
        boolean isAdmin = SecurityUtils.isCurrentUserAdmin();

        if (!isOwner && !isAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Bạn không có quyền xem lịch sử lịch đặt phòng này");
        }

        int validPage = Math.max(0, page);
        int validSize = Math.min(100, Math.max(1, size));
        Pageable pageable = PageRequest.of(validPage, validSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<BookingHistory> historyPage = bookingHistoryRepository.findByBookingIdWithActor(bookingId, pageable);
        return PageResponse.of(historyPage.map(bookingMapper::toHistoryResponse));
    }

    private PageResponse<BookingResponse> queryBookings(BookingLookupRequest request) {
        int page = Math.max(0, request.getPage());
        int size = Math.min(100, Math.max(1, request.getSize()));

        String sortBy = request.getSortBy();
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "startTime";
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(request.getSortDir())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Specification<Booking> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getRoomId() != null) {
                predicates.add(cb.equal(root.get("room").get("roomId"), request.getRoomId()));
            }

            if (request.getUserId() != null) {
                predicates.add(cb.equal(root.get("bookedBy").get("userId"), request.getUserId()));
            }

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            if (request.getFromTime() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("endTime"), request.getFromTime()));
            }

            if (request.getToTime() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startTime"), request.getToTime()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Booking> bookingPage = bookingRepository.findAll(spec, pageable);
        return PageResponse.of(bookingPage.map(bookingMapper::toResponse));
    }
}
