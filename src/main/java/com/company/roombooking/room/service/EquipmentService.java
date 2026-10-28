package com.company.roombooking.room.service;

import com.company.roombooking.common.dto.PageResponse;
import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.common.util.SecurityUtils;
import com.company.roombooking.room.dto.CreateEquipmentRequest;
import com.company.roombooking.room.dto.EquipmentDetailResponse;
import com.company.roombooking.room.dto.EquipmentLookupRequest;
import com.company.roombooking.room.dto.EquipmentResponse;
import com.company.roombooking.room.dto.UpdateEquipmentRequest;
import com.company.roombooking.room.entity.Equipment;
import com.company.roombooking.room.mapper.EquipmentMapper;
import com.company.roombooking.room.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "equipmentId", "code", "nameVi", "nameEn", "createdAt"
    );

    private final EquipmentRepository equipmentRepository;
    private final EquipmentMapper equipmentMapper;

    private void ensureAdmin() {
        if (!SecurityUtils.isCurrentUserAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<EquipmentResponse> getEquipments(EquipmentLookupRequest request) {
        int page = Math.max(0, request.getPage());
        int size = Math.min(100, Math.max(1, request.getSize()));

        String sortBy = request.getSortBy();
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "equipmentId";
        }

        Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDir())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Equipment> equipmentPage = equipmentRepository.findAll(pageable);
        return PageResponse.of(equipmentPage.map(equipmentMapper::toResponse));
    }

    @Transactional
    public EquipmentDetailResponse createEquipment(CreateEquipmentRequest request) {
        ensureAdmin();

        String code = request.getCode().trim().toUpperCase();
        if (equipmentRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException(ErrorCode.EQUIPMENT_CODE_ALREADY_EXISTS);
        }

        Equipment equipment = Equipment.builder()
                .code(code)
                .nameVi(request.getNameVi().trim())
                .nameEn(request.getNameEn().trim())
                .build();

        Equipment saved = equipmentRepository.save(equipment);
        return equipmentMapper.toDetailResponse(saved);
    }

    @Transactional
    public EquipmentDetailResponse updateEquipment(Long equipmentId, UpdateEquipmentRequest request) {
        ensureAdmin();

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND));

        equipment.setNameVi(request.getNameVi().trim());
        equipment.setNameEn(request.getNameEn().trim());

        Equipment updated = equipmentRepository.save(equipment);
        return equipmentMapper.toDetailResponse(updated);
    }
}
