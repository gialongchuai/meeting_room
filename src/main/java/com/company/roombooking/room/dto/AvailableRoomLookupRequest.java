package com.company.roombooking.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Yêu cầu tìm phòng họp trống")
public class AvailableRoomLookupRequest {

    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @Schema(description = "Thời gian bắt đầu (epoch ms UTC)", example = "1788321600000")
    private Long startTime;

    @NotNull(message = "Thời gian kết thúc không được để trống")
    @Schema(description = "Thời gian kết thúc (epoch ms UTC)", example = "1788325200000")
    private Long endTime;

    @Min(value = 1, message = "Sức chứa tối thiểu phải >= 1")
    @Schema(description = "Sức chứa tối thiểu yêu cầu", example = "8")
    private Integer minCapacity;

    @Schema(description = "Danh sách ID các tiện ích yêu cầu phòng phải có đủ tất cả", example = "[1, 2]")
    private List<Long> equipmentIds;

    @Min(value = 0, message = "Chỉ số trang phải >= 0")
    @Builder.Default
    @Schema(description = "Chỉ số trang (bắt đầu từ 0)", example = "0")
    private int page = 0;

    @Min(value = 1, message = "Kích thước trang phải >= 1")
    @Max(value = 100, message = "Kích thước trang tối đa là 100")
    @Builder.Default
    @Schema(description = "Số bản ghi trên mỗi trang (tối đa 100)", example = "10")
    private int size = 10;
}
