package com.company.roombooking.booking.dto;

import com.company.roombooking.booking.entity.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Schema(description = "Yêu cầu tra cứu danh sách lịch đặt phòng")
public class BookingLookupRequest {

    @Schema(description = "Lọc theo ID phòng họp", example = "3")
    private Long roomId;

    @Schema(description = "Lọc theo ID người đặt phòng", example = "17")
    private Long userId;

    @Schema(description = "Lọc theo trạng thái (CONFIRMED, CANCELLED)", example = "CONFIRMED")
    private BookingStatus status;

    @Schema(description = "Từ thời điểm (epoch ms UTC)", example = "1788321600000")
    private Long fromTime;

    @Schema(description = "Đến thời điểm (epoch ms UTC)", example = "1788408000000")
    private Long toTime;

    @Min(value = 0, message = "Chỉ số trang phải >= 0")
    @Builder.Default
    @Schema(description = "Chỉ số trang (bắt đầu từ 0)", example = "0")
    private int page = 0;

    @Min(value = 1, message = "Kích thước trang phải >= 1")
    @Max(value = 100, message = "Kích thước trang tối đa là 100")
    @Builder.Default
    @Schema(description = "Số bản ghi trên mỗi trang (tối đa 100)", example = "10")
    private int size = 10;

    @Builder.Default
    @Schema(description = "Trường sắp xếp (startTime, createdAt)", example = "startTime")
    private String sortBy = "startTime";

    @Builder.Default
    @Schema(description = "Hướng sắp xếp (ASC hoặc DESC)", example = "DESC")
    private String sortDir = "DESC";
}
