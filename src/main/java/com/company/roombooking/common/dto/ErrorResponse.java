package com.company.roombooking.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Phản hồi lỗi chuẩn")
public class ErrorResponse {

    @Schema(description = "Mã lỗi chuẩn", example = "ROOM_ALREADY_BOOKED")
    private String code;

    @Schema(description = "Thông điệp mô tả lỗi", example = "Phòng đã được đặt trong khung giờ này")
    private String message;

    @Schema(description = "Thời điểm xảy ra lỗi (epoch ms UTC)", example = "1788321600000")
    private Long timestamp;

    @Schema(description = "Đường dẫn API gây ra lỗi", example = "/api/v1/bookings")
    private String path;

    @Schema(description = "Chi tiết lỗi (nếu có)", example = "[]")
    @Builder.Default
    private List<String> details = new ArrayList<>();
}
