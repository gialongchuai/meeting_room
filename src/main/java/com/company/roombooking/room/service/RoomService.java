package com.company.roombooking.room.service;

import com.company.roombooking.common.dto.PageResponse;
import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.common.util.SecurityUtils;
import com.company.roombooking.room.dto.CreateRoomRequest;
import com.company.roombooking.room.dto.RoomDetailResponse;
import com.company.roombooking.room.dto.RoomLookupRequest;
import com.company.roombooking.room.dto.RoomResponse;
import com.company.roombooking.room.dto.UpdateRoomRequest;
import com.company.roombooking.room.dto.UpdateRoomStatusRequest;
import com.company.roombooking.room.entity.Equipment;
import com.company.roombooking.room.entity.Room;
import com.company.roombooking.room.entity.RoomStatus;
import com.company.roombooking.room.mapper.RoomMapper;
import com.company.roombooking.room.repository.EquipmentRepository;
import com.company.roombooking.room.repository.RoomRepository;
import com.company.roombooking.room.specification.RoomSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoomService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "roomId", "code", "nameVi", "nameEn", "capacity", "floor", "status", "createdAt"
    );

    private final RoomRepository roomRepository;
    private final EquipmentRepository equipmentRepository;
    private final RoomMapper roomMapper;
    private final RoomBookingBridge roomBookingBridge;

    private void ensureAdmin() {
        if (!SecurityUtils.isCurrentUserAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<RoomResponse> getRooms(RoomLookupRequest request) {
        int page = Math.max(0, request.getPage());
        int size = Math.min(100, Math.max(1, request.getSize()));

        String sortBy = request.getSortBy();
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "code";
        }

        Sort.Direction direction = "DESC".equalsIgnoreCase(request.getSortDir())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<Room> roomPage = roomRepository.findAll(RoomSpecification.filter(request), pageable);
        return PageResponse.of(roomPage.map(roomMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public RoomDetailResponse getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        return roomMapper.toDetailResponse(room);
    }

    @Transactional
    public RoomDetailResponse createRoom(CreateRoomRequest request) {
        ensureAdmin();

        String code = request.getCode().trim().toUpperCase();
        if (roomRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException(ErrorCode.ROOM_CODE_ALREADY_EXISTS);
        }

        Set<Equipment> equipments = resolveEquipments(request.getEquipmentIds());

        Room room = Room.builder()
                .code(code)
                .nameVi(request.getNameVi().trim())
                .nameEn(request.getNameEn().trim())
                .descriptionVi(StringUtils.hasText(request.getDescriptionVi()) ? request.getDescriptionVi().trim() : null)
                .descriptionEn(StringUtils.hasText(request.getDescriptionEn()) ? request.getDescriptionEn().trim() : null)
                .capacity(request.getCapacity())
                .floor(StringUtils.hasText(request.getFloor()) ? request.getFloor().trim() : null)
                .status(RoomStatus.ACTIVE)
                .equipments(equipments)
                .build();

        Room saved = roomRepository.save(room);
        return roomMapper.toDetailResponse(saved);
    }

    @Transactional
    public RoomDetailResponse updateRoom(Long roomId, UpdateRoomRequest request) {
        ensureAdmin();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        // Rule 6.4: Giảm capacity xuống thấp hơn attendee_count của booking tương lai -> 409
        if (request.getCapacity() < room.getCapacity()) {
            long now = System.currentTimeMillis();
            if (roomBookingBridge.existsFutureBookingWithMoreAttendees(roomId, request.getCapacity(), now)) {
                throw new BusinessException(ErrorCode.CAPACITY_CONFLICTS_WITH_BOOKINGS);
            }
        }

        Set<Equipment> equipments = resolveEquipments(request.getEquipmentIds());

        room.setNameVi(request.getNameVi().trim());
        room.setNameEn(request.getNameEn().trim());
        room.setDescriptionVi(StringUtils.hasText(request.getDescriptionVi()) ? request.getDescriptionVi().trim() : null);
        room.setDescriptionEn(StringUtils.hasText(request.getDescriptionEn()) ? request.getDescriptionEn().trim() : null);
        room.setCapacity(request.getCapacity());
        room.setFloor(StringUtils.hasText(request.getFloor()) ? request.getFloor().trim() : null);
        room.setEquipments(equipments);

        Room updated = roomRepository.save(room);
        return roomMapper.toDetailResponse(updated);
    }

    @Transactional
    public RoomDetailResponse updateStatus(Long roomId, UpdateRoomStatusRequest request) {
        ensureAdmin();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        RoomStatus newStatus = request.getStatus();
        if ((newStatus == RoomStatus.MAINTENANCE || newStatus == RoomStatus.INACTIVE) && room.getStatus() != newStatus) {
            long now = System.currentTimeMillis();
            long futureBookings = roomBookingBridge.countFutureConfirmedBookings(roomId, now);

            if (futureBookings > 0) {
                if (!Boolean.TRUE.equals(request.getCancelFutureBookings())) {
                    throw new BusinessException(ErrorCode.ROOM_HAS_FUTURE_BOOKINGS, new Object[]{futureBookings});
                }
                if (!StringUtils.hasText(request.getReason())) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Lý do hủy là bắt buộc khi tự động hủy các lịch đặt phòng");
                }
                Long adminId = SecurityUtils.getCurrentUserId();
                roomBookingBridge.cancelAllFutureBookingsForRoom(roomId, request.getReason().trim(), adminId);
            }
        }

        room.setStatus(newStatus);
        Room updated = roomRepository.save(room);
        return roomMapper.toDetailResponse(updated);
    }

    private Set<Equipment> resolveEquipments(List<Long> equipmentIds) {
        if (equipmentIds == null || equipmentIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Equipment> found = equipmentRepository.findByEquipmentIdIn(equipmentIds);
        if (found.size() != equipmentIds.size()) {
            throw new BusinessException(ErrorCode.EQUIPMENT_NOT_FOUND, "Một hoặc nhiều tiện ích không tồn tại");
        }
        return new HashSet<>(found);
    }
}
