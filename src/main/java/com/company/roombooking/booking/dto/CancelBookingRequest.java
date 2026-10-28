package com.company.roombooking.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Yêu cầu hủy lịch đặt phòng")
public class CancelBookingRequest {

    @Size(max = 255, message = "Lý do hủy tối đa 255 ký tự")
    @Schema(description = "Lý do hủy lịch (bắt buộc nếu Admin hủy lịch của người khác)", example = "Thay đổi kế hoạch cuộc họp")
    private String reason;
}
