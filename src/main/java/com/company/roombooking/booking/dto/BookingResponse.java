package com.company.roombooking.booking.dto;

import com.company.roombooking.auth.dto.UserSummaryResponse;
import com.company.roombooking.booking.entity.BookingStatus;
import com.company.roombooking.common.dto.LocalizedText;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin cơ bản lịch đặt phòng")
public class BookingResponse {

    @Schema(description = "ID lịch đặt phòng", example = "5001")
    private Long bookingId;

    @Schema(description = "ID phòng", example = "3")
    private Long roomId;

    @Schema(description = "Tên phòng đa ngôn ngữ")
    private LocalizedText roomName;

    @Schema(description = "Tiêu đề cuộc họp", example = "Sprint planning")
    private String title;

    @Schema(description = "Số người tham gia", example = "8")
    private int attendeeCount;

    @Schema(description = "Thời gian bắt đầu (epoch ms UTC)", example = "1788321600000")
    private Long startTime;

    @Schema(description = "Thời gian kết thúc (epoch ms UTC)", example = "1788325200000")
    private Long endTime;

    @Schema(description = "Trạng thái lịch đặt", example = "CONFIRMED")
    private BookingStatus status;

    @Schema(description = "Người đặt phòng")
    private UserSummaryResponse bookedBy;
}
