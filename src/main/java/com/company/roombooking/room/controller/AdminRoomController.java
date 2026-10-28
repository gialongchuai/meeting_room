package com.company.roombooking.room.controller;

import com.company.roombooking.room.dto.CreateRoomRequest;
import com.company.roombooking.room.dto.RoomDetailResponse;
import com.company.roombooking.room.dto.UpdateRoomRequest;
import com.company.roombooking.room.dto.UpdateRoomStatusRequest;
import com.company.roombooking.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Quản trị phòng họp (ADMIN)", description = "Các API quản lý phòng họp dành cho quản trị viên")
public class AdminRoomController {

    private final RoomService roomService;

    @PostMapping
    @Operation(summary = "Tạo phòng họp", description = "Admin tạo mới một phòng họp")
    public ResponseEntity<RoomDetailResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request));
    }

    @PutMapping("/{roomId}")
    @Operation(summary = "Cập nhật phòng họp", description = "Admin cập nhật thông tin phòng họp")
    public ResponseEntity<RoomDetailResponse> updateRoom(@PathVariable Long roomId,
                                                        @Valid @RequestBody UpdateRoomRequest request) {
        return ResponseEntity.ok(roomService.updateRoom(roomId, request));
    }

    @PatchMapping("/{roomId}/status")
    @Operation(summary = "Đổi trạng thái phòng họp", description = "Admin đổi trạng thái phòng (ACTIVE, MAINTENANCE, INACTIVE)")
    public ResponseEntity<RoomDetailResponse> updateStatus(@PathVariable Long roomId,
                                                          @Valid @RequestBody UpdateRoomStatusRequest request) {
        return ResponseEntity.ok(roomService.updateStatus(roomId, request));
    }
}
