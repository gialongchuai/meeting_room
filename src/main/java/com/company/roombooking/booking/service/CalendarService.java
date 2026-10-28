package com.company.roombooking.booking.service;

import com.company.roombooking.auth.dto.UserSummaryResponse;
import com.company.roombooking.booking.dto.CalendarEntryResponse;
import com.company.roombooking.booking.dto.RoomCalendarResponse;
import com.company.roombooking.booking.entity.Booking;
import com.company.roombooking.booking.repository.BookingRepository;
import com.company.roombooking.common.dto.LocalizedText;
import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.room.entity.Room;
import com.company.roombooking.room.entity.RoomStatus;
import com.company.roombooking.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service xem lịch phòng họp.
 * LƯU Ý KỸ THUẬT: Các API xem lịch ở đây KHÔNG phân trang vì kết quả đã bị giới hạn chặt chẽ
 * bởi khoảng thời gian tối đa 31 ngày (CALENDAR_RANGE_TOO_LARGE nếu vượt quá) và số lượng phòng
 * công ty quy mô nhỏ (~10 phòng). Quyết định này được nêu rõ trong mục 5.4 và README.md.
 */
@Service
@RequiredArgsConstructor
public class CalendarService {

    private static final long MAX_CALENDAR_RANGE_MILLIS = 31L * 24 * 60 * 60 * 1000L;

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public RoomCalendarResponse getRoomCalendar(Long roomId, Long fromTime, Long toTime) {
        validateTimeRange(fromTime, toTime);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        List<Booking> bookings = bookingRepository.findRoomCalendar(roomId, fromTime, toTime);
        List<CalendarEntryResponse> entries = bookings.stream()
                .map(this::toCalendarEntry)
                .toList();

        return RoomCalendarResponse.builder()
                .roomId(room.getRoomId())
                .roomCode(room.getCode())
                .roomName(LocalizedText.of(room.getNameVi(), room.getNameEn()))
                .capacity(room.getCapacity())
                .entries(entries)
                .build();
    }

    @Transactional(readOnly = true)
    public List<RoomCalendarResponse> getCompanyCalendar(Long fromTime, Long toTime) {
        validateTimeRange(fromTime, toTime);

        List<Room> allRooms = roomRepository.findAll(Sort.by(Sort.Direction.ASC, "code"));
        List<Booking> allBookings = bookingRepository.findCompanyCalendar(fromTime, toTime);

        Map<Long, List<CalendarEntryResponse>> entriesByRoom = new HashMap<>();
        for (Booking b : allBookings) {
            entriesByRoom.computeIfAbsent(b.getRoom().getRoomId(), k -> new ArrayList<>())
                    .add(toCalendarEntry(b));
        }

        List<RoomCalendarResponse> result = new ArrayList<>();
        for (Room room : allRooms) {
            if (room.getStatus() == RoomStatus.INACTIVE) {
                continue;
            }
            List<CalendarEntryResponse> roomEntries = entriesByRoom.getOrDefault(room.getRoomId(), Collections.emptyList());
            result.add(RoomCalendarResponse.builder()
                    .roomId(room.getRoomId())
                    .roomCode(room.getCode())
                    .roomName(LocalizedText.of(room.getNameVi(), room.getNameEn()))
                    .capacity(room.getCapacity())
                    .entries(roomEntries)
                    .build());
        }

        return result;
    }

    private void validateTimeRange(Long fromTime, Long toTime) {
        if (fromTime == null || toTime == null || toTime <= fromTime) {
            throw new BusinessException(ErrorCode.INVALID_TIME_RANGE);
        }
        if (toTime - fromTime > MAX_CALENDAR_RANGE_MILLIS) {
            throw new BusinessException(ErrorCode.CALENDAR_RANGE_TOO_LARGE);
        }
    }

    private CalendarEntryResponse toCalendarEntry(Booking booking) {
        return CalendarEntryResponse.builder()
                .bookingId(booking.getBookingId())
                .title(booking.getTitle())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .attendeeCount(booking.getAttendeeCount())
                .bookedBy(UserSummaryResponse.builder()
                        .userId(booking.getBookedBy().getUserId())
                        .email(booking.getBookedBy().getEmail())
                        .fullName(booking.getBookedBy().getFullName())
                        .role(booking.getBookedBy().getRole() != null ? booking.getBookedBy().getRole().name() : null)
                        .build())
                .build();
    }
}
