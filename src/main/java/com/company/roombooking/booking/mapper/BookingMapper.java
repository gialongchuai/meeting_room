package com.company.roombooking.booking.mapper;

import com.company.roombooking.auth.dto.UserSummaryResponse;
import com.company.roombooking.booking.dto.BookingDetailResponse;
import com.company.roombooking.booking.dto.BookingHistoryResponse;
import com.company.roombooking.booking.dto.BookingResponse;
import com.company.roombooking.booking.entity.Booking;
import com.company.roombooking.booking.entity.BookingHistory;
import com.company.roombooking.common.dto.LocalizedText;
import com.company.roombooking.room.entity.Room;
import com.company.roombooking.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "roomId", source = "room.roomId")
    @Mapping(target = "roomName", expression = "java(toLocalizedRoomName(booking.getRoom()))")
    @Mapping(target = "bookedBy", expression = "java(toUserSummary(booking.getBookedBy()))")
    BookingResponse toResponse(Booking booking);

    List<BookingResponse> toResponseList(List<Booking> bookings);

    @Mapping(target = "roomId", source = "room.roomId")
    @Mapping(target = "roomCode", source = "room.code")
    @Mapping(target = "roomName", expression = "java(toLocalizedRoomName(booking.getRoom()))")
    @Mapping(target = "bookedBy", expression = "java(toUserSummary(booking.getBookedBy()))")
    @Mapping(target = "cancelledBy", expression = "java(toUserSummary(booking.getCancelledBy()))")
    BookingDetailResponse toDetailResponse(Booking booking);

    @Mapping(target = "historyId", source = "id")
    @Mapping(target = "bookingId", source = "booking.bookingId")
    @Mapping(target = "actor", expression = "java(toUserSummary(history.getActor()))")
    BookingHistoryResponse toHistoryResponse(BookingHistory history);

    List<BookingHistoryResponse> toHistoryResponseList(List<BookingHistory> histories);

    default LocalizedText toLocalizedRoomName(Room room) {
        if (room == null) {
            return null;
        }
        return LocalizedText.of(room.getNameVi(), room.getNameEn());
    }

    default UserSummaryResponse toUserSummary(User user) {
        if (user == null) {
            return null;
        }
        return UserSummaryResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .build();
    }
}
