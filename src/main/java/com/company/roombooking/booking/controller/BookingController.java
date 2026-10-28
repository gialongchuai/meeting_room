package com.company.roombooking.booking.controller;

import com.company.roombooking.booking.dto.BookingDetailResponse;
import com.company.roombooking.booking.dto.BookingHistoryResponse;
import com.company.roombooking.booking.dto.BookingLookupRequest;
import com.company.roombooking.booking.dto.BookingResponse;
import com.company.roombooking.booking.dto.CancelBookingRequest;
import com.company.roombooking.booking.dto.CreateBookingRequest;
import com.company.roombooking.booking.dto.UpdateBookingRequest;
import com.company.roombooking.booking.service.BookingQueryService;
import com.company.roombooking.booking.service.BookingService;
import com.company.roombooking.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Đặt phòng", description = "Các API đặt phòng, đổi lịch, hủy và tra cứu lịch cá nhân")
public class BookingController {

    private final BookingService bookingService;
    private final BookingQueryService bookingQueryService;

    @PostMapping
    @Operation(summary = "Đặt phòng họp", description = "Tạo một lịch đặt phòng họp mới")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Lịch đặt của tôi", description = "Danh sách các lịch đặt phòng do tôi tạo có phân trang")
    public ResponseEntity<PageResponse<BookingResponse>> getMyBookings(@Valid @ModelAttribute BookingLookupRequest request) {
        return ResponseEntity.ok(bookingQueryService.getMyBookings(request));
    }

    @GetMapping("/{bookingId:[0-9]+}")
    @Operation(summary = "Chi tiết lịch đặt phòng", description = "Xem chi tiết lịch đặt phòng (Chủ booking hoặc ADMIN)")
    public ResponseEntity<BookingDetailResponse> getBookingById(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingQueryService.getBookingById(bookingId));
    }

    @PutMapping("/{bookingId:[0-9]+}")
    @Operation(summary = "Đổi lịch / thông tin đặt phòng", description = "Chủ booking hoặc ADMIN đổi lịch họp chưa bắt đầu")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Long bookingId,
                                                        @Valid @RequestBody UpdateBookingRequest request) {
        return ResponseEntity.ok(bookingService.updateBooking(bookingId, request));
    }

    @PostMapping("/{bookingId:[0-9]+}/cancel")
    @Operation(summary = "Hủy lịch đặt phòng", description = "Hủy lịch họp (Chủ booking hoặc ADMIN với lý do bắt buộc)")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId,
                                             @RequestBody(required = false) CancelBookingRequest request) {
        bookingService.cancelBooking(bookingId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{bookingId:[0-9]+}/history")
    @Operation(summary = "Lịch sử thay đổi lịch đặt", description = "Xem lịch sử audit các lần đổi/hủy lịch đặt phòng")
    public ResponseEntity<PageResponse<BookingHistoryResponse>> getBookingHistory(
            @PathVariable Long bookingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookingQueryService.getBookingHistory(bookingId, page, size));
    }
}
