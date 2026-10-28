package com.company.roombooking.room.controller;

import com.company.roombooking.room.dto.CreateEquipmentRequest;
import com.company.roombooking.room.dto.EquipmentDetailResponse;
import com.company.roombooking.room.dto.UpdateEquipmentRequest;
import com.company.roombooking.room.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/equipments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Quản trị tiện ích thiết bị (ADMIN)", description = "Các API quản lý tiện ích thiết bị phòng họp dành cho quản trị viên")
public class AdminEquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping
    @Operation(summary = "Tạo tiện ích", description = "Admin tạo mới một tiện ích thiết bị")
    public ResponseEntity<EquipmentDetailResponse> createEquipment(@Valid @RequestBody CreateEquipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.createEquipment(request));
    }

    @PutMapping("/{equipmentId}")
    @Operation(summary = "Cập nhật tiện ích", description = "Admin cập nhật thông tin tiện ích thiết bị")
    public ResponseEntity<EquipmentDetailResponse> updateEquipment(@PathVariable Long equipmentId,
                                                                  @Valid @RequestBody UpdateEquipmentRequest request) {
        return ResponseEntity.ok(equipmentService.updateEquipment(equipmentId, request));
    }
}
