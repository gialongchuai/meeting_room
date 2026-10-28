package com.company.roombooking.room.dto;

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
@Schema(description = "Yêu cầu danh sách tiện ích thiết bị")
public class EquipmentLookupRequest {

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
    @Schema(description = "Trường sắp xếp (equipmentId, code, nameVi, nameEn, createdAt)", example = "equipmentId")
    private String sortBy = "equipmentId";

    @Builder.Default
    @Schema(description = "Hướng sắp xếp (ASC hoặc DESC)", example = "ASC")
    private String sortDir = "ASC";
}
