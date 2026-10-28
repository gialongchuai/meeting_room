package com.company.roombooking.booking.controller;

import com.company.roombooking.booking.dto.BookingLookupRequest;
import com.company.roombooking.booking.dto.BookingResponse;
import com.company.roombooking.booking.service.BookingQueryService;
import com.company.roombooking.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Quản trị lịch đặt phòng (ADMIN)", description = "Các API tra cứu toàn bộ lịch đặt phòng công ty dành cho quản trị viên")
public class AdminBookingController {

    private final BookingQueryService bookingQueryService;

    @GetMapping
    @Operation(summary = "Xem toàn bộ lịch đặt phòng", description = "Tìm kiếm và phân trang mọi lịch đặt phòng trên hệ thống")
    public ResponseEntity<PageResponse<BookingResponse>> getAllBookings(@Valid @ModelAttribute BookingLookupRequest request) {
        return ResponseEntity.ok(bookingQueryService.getAllBookings(request));
    }
}
