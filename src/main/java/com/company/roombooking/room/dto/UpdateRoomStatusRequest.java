package com.company.roombooking.room.dto;

import com.company.roombooking.room.entity.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Yêu cầu thay đổi trạng thái phòng họp (ADMIN)")
public class UpdateRoomStatusRequest {

    @NotNull(message = "Trạng thái phòng không được để trống")
    @Schema(description = "Trạng thái mới (ACTIVE, MAINTENANCE, INACTIVE)", example = "MAINTENANCE")
    private RoomStatus status;

    @Schema(description = "Đồng ý tự động hủy các lịch đặt phòng trong tương lai nếu có", example = "true")
    private Boolean cancelFutureBookings;

    @Schema(description = "Lý do hủy lịch đặt phòng tương lai (bắt buộc nếu cancelFutureBookings=true)", example = "Bảo trì định kỳ hệ thống điều hòa")
    private String reason;
}
