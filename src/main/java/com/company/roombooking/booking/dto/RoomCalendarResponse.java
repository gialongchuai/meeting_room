package com.company.roombooking.booking.dto;

import com.company.roombooking.common.dto.LocalizedText;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Lịch biểu các cuộc họp của phòng")
public class RoomCalendarResponse {

    @Schema(description = "ID phòng", example = "3")
    private Long roomId;

    @Schema(description = "Mã phòng", example = "HN-3F-01")
    private String roomCode;

    @Schema(description = "Tên phòng đa ngôn ngữ")
    private LocalizedText roomName;

    @Schema(description = "Sức chứa tối đa của phòng", example = "15")
    private int capacity;

    @Schema(description = "Danh sách các cuộc họp đã xác nhận trong khoảng thời gian")
    private List<CalendarEntryResponse> entries;
}
