package com.company.roombooking.room.dto;

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
@Schema(description = "Thông tin chi tiết tiện ích thiết bị")
public class EquipmentDetailResponse {

    @Schema(description = "ID thiết bị", example = "1")
    private Long equipmentId;

    @Schema(description = "Mã thiết bị", example = "PROJECTOR")
    private String code;

    @Schema(description = "Tên thiết bị đa ngôn ngữ")
    private LocalizedText name;

    @Schema(description = "Thời điểm tạo (epoch ms UTC)", example = "1704067200000")
    private Long createdAt;

    @Schema(description = "Thời điểm cập nhật (epoch ms UTC)", example = "1704067200000")
    private Long updatedAt;
}
