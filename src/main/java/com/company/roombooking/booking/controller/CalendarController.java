package com.company.roombooking.booking.controller;

import com.company.roombooking.booking.dto.RoomCalendarResponse;
import com.company.roombooking.booking.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
@Tag(name = "Lịch biểu công ty", description = "Các API xem lịch toàn thể phòng họp công ty")
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping
    @Operation(summary = "Lịch toàn bộ phòng họp", description = "Xem lịch họp của toàn bộ phòng họp công ty theo nhóm phòng (tối đa 31 ngày)")
    public ResponseEntity<List<RoomCalendarResponse>> getCompanyCalendar(
            @RequestParam Long fromTime,
            @RequestParam Long toTime) {
        return ResponseEntity.ok(calendarService.getCompanyCalendar(fromTime, toTime));
    }
}
