package com.company.roombooking.room.controller;

import com.company.roombooking.common.dto.PageResponse;
import com.company.roombooking.room.dto.EquipmentLookupRequest;
import com.company.roombooking.room.dto.EquipmentResponse;
import com.company.roombooking.room.service.EquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/equipments")
@RequiredArgsConstructor
@Tag(name = "Tiện ích thiết bị", description = "Các API tra cứu danh mục tiện ích thiết bị phòng họp")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    @Operation(summary = "Danh mục tiện ích", description = "Lấy danh sách các tiện ích thiết bị có phân trang")
    public ResponseEntity<PageResponse<EquipmentResponse>> getEquipments(@Valid @ModelAttribute EquipmentLookupRequest request) {
        return ResponseEntity.ok(equipmentService.getEquipments(request));
    }
}
