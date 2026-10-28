package com.company.roombooking.booking.dto;

import com.company.roombooking.auth.dto.UserSummaryResponse;
import com.company.roombooking.booking.entity.BookingAction;
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
@Schema(description = "Lịch sử thay đổi lịch đặt phòng (Audit)")
public class BookingHistoryResponse {

    @Schema(description = "ID bản ghi lịch sử", example = "101")
    private Long historyId;

    @Schema(description = "ID lịch đặt phòng", example = "5001")
    private Long bookingId;

    @Schema(description = "Hành động thực hiện (CREATED, RESCHEDULED, CANCELLED)", example = "CREATED")
    private BookingAction action;

    @Schema(description = "Thời gian bắt đầu cũ (epoch ms UTC)", example = "1788321600000")
    private Long oldStartTime;

    @Schema(description = "Thời gian kết thúc cũ (epoch ms UTC)", example = "1788325200000")
    private Long oldEndTime;

    @Schema(description = "Thời gian bắt đầu mới (epoch ms UTC)", example = "1788325200000")
    private Long newStartTime;

    @Schema(description = "Thời gian kết thúc mới (epoch ms UTC)", example = "1788328800000")
    private Long newEndTime;

    @Schema(description = "Người thực hiện hành động")
    private UserSummaryResponse actor;

    @Schema(description = "Ghi chú bổ sung", example = "Đặt phòng mới thành công")
    private String note;

    @Schema(description = "Thời điểm thực hiện hành động (epoch ms UTC)", example = "1788321600000")
    private Long createdAt;
}
