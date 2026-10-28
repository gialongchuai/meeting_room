package com.company.roombooking.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Yêu cầu đặt phòng họp")
public class CreateBookingRequest {

    @NotNull(message = "ID phòng không được để trống")
    @Schema(description = "ID phòng họp cần đặt", example = "3")
    private Long roomId;

    @NotBlank(message = "Tiêu đề cuộc họp không được để trống")
    @Size(max = 200, message = "Tiêu đề cuộc họp tối đa 200 ký tự")
    @Schema(description = "Tiêu đề cuộc họp", example = "Sprint planning")
    private String title;

    @Size(max = 1000, message = "Mô tả tối đa 1000 ký tự")
    @Schema(description = "Mô tả chi tiết nội dung cuộc họp", example = "Họp kế hoạch sprint 24")
    private String description;

    @Min(value = 1, message = "Số lượng người tham gia tối thiểu là 1")
    @Schema(description = "Số lượng người tham gia cuộc họp", example = "8")
    private int attendeeCount;

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @Schema(description = "Thời gian bắt đầu (epoch ms UTC)", example = "1788321600000")
    private Long startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    @Schema(description = "Thời gian kết thúc (epoch ms UTC)", example = "1788325200000")
    private Long endTime;
}
