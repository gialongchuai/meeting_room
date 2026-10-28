package com.company.roombooking.room.dto;

import com.company.roombooking.common.dto.LocalizedText;
import com.company.roombooking.room.entity.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Thông tin phòng họp")
public class RoomResponse {

    @Schema(description = "ID phòng họp", example = "1")
    private Long roomId;

    @Schema(description = "Mã phòng họp", example = "HN-3F-01")
    private String code;

    @Schema(description = "Tên phòng họp đa ngôn ngữ")
    private LocalizedText name;

    @Schema(description = "Mô tả phòng họp đa ngôn ngữ")
    private LocalizedText description;

    @Schema(description = "Sức chứa tối đa", example = "15")
    private int capacity;

    @Schema(description = "Tầng / Vị trí", example = "3F")
    private String floor;

    @Schema(description = "Trạng thái phòng", example = "ACTIVE")
    private RoomStatus status;

    @Schema(description = "Danh sách tiện ích trang bị")
    private List<EquipmentResponse> equipments;
}
