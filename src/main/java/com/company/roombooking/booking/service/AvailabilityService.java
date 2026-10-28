package com.company.roombooking.booking.service;

import com.company.roombooking.booking.entity.Booking;
import com.company.roombooking.booking.entity.BookingStatus;
import com.company.roombooking.common.dto.PageResponse;
import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.room.dto.AvailableRoomLookupRequest;
import com.company.roombooking.room.dto.RoomResponse;
import com.company.roombooking.room.entity.Equipment;
import com.company.roombooking.room.entity.Room;
import com.company.roombooking.room.entity.RoomStatus;
import com.company.roombooking.room.mapper.RoomMapper;
import com.company.roombooking.room.repository.RoomRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
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

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    @Transactional(readOnly = true)
    public PageResponse<RoomResponse> findAvailableRooms(AvailableRoomLookupRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null
                || request.getEndTime() <= request.getStartTime()) {
            throw new BusinessException(ErrorCode.INVALID_TIME_RANGE);
        }

        int page = Math.max(0, request.getPage());
        int size = Math.min(100, Math.max(1, request.getSize()));

        // Sắp xếp theo sức chứa nhỏ nhất đủ dùng trước, sau đó theo mã phòng
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "capacity").and(Sort.by(Sort.Direction.ASC, "code")));

        Specification<Room> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Chỉ phòng ACTIVE
            predicates.add(cb.equal(root.get("status"), RoomStatus.ACTIVE));

            // 2. Sức chứa tối thiểu (nếu có)
            if (request.getMinCapacity() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("capacity"), request.getMinCapacity()));
            }

            // 3. Có đủ TẤT CẢ các tiện ích yêu cầu
            if (request.getEquipmentIds() != null && !request.getEquipmentIds().isEmpty()) {
                Subquery<Long> eqSub = query.subquery(Long.class);
                Root<Room> subRoot = eqSub.correlate(root);
                Join<Room, Equipment> eqJoin = subRoot.join("equipments");
                eqSub.select(cb.countDistinct(eqJoin.get("equipmentId")))
                        .where(eqJoin.get("equipmentId").in(request.getEquipmentIds()));
                predicates.add(cb.equal(eqSub, (long) request.getEquipmentIds().size()));
            }

            // 4. Không có booking CONFIRMED giao với [startTime, endTime)
            Subquery<Long> bookingSub = query.subquery(Long.class);
            Root<Booking> bRoot = bookingSub.from(Booking.class);
            bookingSub.select(bRoot.get("bookingId"))
                    .where(
                            cb.equal(bRoot.get("room"), root),
                            cb.equal(bRoot.get("status"), BookingStatus.CONFIRMED),
                            cb.lessThan(bRoot.get("startTime"), request.getEndTime()),
                            cb.greaterThan(bRoot.get("endTime"), request.getStartTime())
                    );
            predicates.add(cb.not(cb.exists(bookingSub)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Room> roomPage = roomRepository.findAll(spec, pageable);
        return PageResponse.of(roomPage.map(roomMapper::toResponse));
    }
}
