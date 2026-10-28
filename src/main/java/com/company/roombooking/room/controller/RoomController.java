package com.company.roombooking.room.controller;

import com.company.roombooking.booking.dto.RoomCalendarResponse;
import com.company.roombooking.booking.service.AvailabilityService;
import com.company.roombooking.booking.service.CalendarService;
import com.company.roombooking.common.dto.PageResponse;
import com.company.roombooking.room.dto.AvailableRoomLookupRequest;
import com.company.roombooking.room.dto.RoomDetailResponse;
import com.company.roombooking.room.dto.RoomLookupRequest;
import com.company.roombooking.room.dto.RoomResponse;
import com.company.roombooking.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Phòng họp", description = "Các API tra cứu thông tin phòng họp, lịch phòng và tìm phòng trống")
public class RoomController {

    private final RoomService roomService;
    private final AvailabilityService availabilityService;
    private final CalendarService calendarService;

    @GetMapping
    @Operation(summary = "Danh sách phòng họp", description = "Tìm kiếm và phân trang danh sách phòng họp theo điều kiện")
    public ResponseEntity<PageResponse<RoomResponse>> getRooms(@Valid @ModelAttribute RoomLookupRequest request) {
        return ResponseEntity.ok(roomService.getRooms(request));
    }

    @GetMapping("/available")
    @Operation(summary = "Tìm phòng trống", description = "Tìm danh sách phòng trống theo khoảng thời gian, sức chứa và tiện ích")
    public ResponseEntity<PageResponse<RoomResponse>> findAvailableRooms(@Valid @ModelAttribute AvailableRoomLookupRequest request) {
        return ResponseEntity.ok(availabilityService.findAvailableRooms(request));
    }

    @GetMapping("/{roomId:[0-9]+}")
    @Operation(summary = "Chi tiết phòng họp", description = "Xem chi tiết một phòng họp và danh sách tiện ích")
    public ResponseEntity<RoomDetailResponse> getRoomById(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getRoomById(roomId));
    }

    @GetMapping("/{roomId:[0-9]+}/calendar")
    @Operation(summary = "Lịch một phòng họp", description = "Xem lịch họp của một phòng cụ thể trong khoảng thời gian (tối đa 31 ngày)")
    public ResponseEntity<RoomCalendarResponse> getRoomCalendar(
            @PathVariable Long roomId,
            @RequestParam Long fromTime,
            @RequestParam Long toTime) {
        return ResponseEntity.ok(calendarService.getRoomCalendar(roomId, fromTime, toTime));
    }
}
