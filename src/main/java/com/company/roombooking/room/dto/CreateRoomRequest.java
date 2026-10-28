package com.company.roombooking.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
@Schema(description = "Yêu cầu tạo mới phòng họp (ADMIN)")
public class CreateRoomRequest {

    @NotBlank(message = "Mã phòng không được để trống")
    @Size(max = 20, message = "Mã phòng tối đa 20 ký tự")
    @Schema(description = "Mã phòng (duy nhất)", example = "HN-3F-01")
    private String code;

    @NotBlank(message = "Tên phòng tiếng Việt không được để trống")
    @Size(max = 100, message = "Tên phòng tiếng Việt tối đa 100 ký tự")
    @Schema(description = "Tên phòng tiếng Việt", example = "Phòng Hà Nội")
    private String nameVi;

    @NotBlank(message = "Tên phòng tiếng Anh không được để trống")
    @Size(max = 100, message = "Tên phòng tiếng Anh tối đa 100 ký tự")
    @Schema(description = "Tên phòng tiếng Anh", example = "Hanoi Room")
    private String nameEn;

    @Size(max = 500, message = "Mô tả tiếng Việt tối đa 500 ký tự")
    @Schema(description = "Mô tả tiếng Việt", example = "Phòng họp lớn tầng 3 có máy chiếu")
    private String descriptionVi;

    @Size(max = 500, message = "Mô tả tiếng Anh tối đa 500 ký tự")
    @Schema(description = "Mô tả tiếng Anh", example = "Large meeting room on 3rd floor with projector")
    private String descriptionEn;

    @Min(value = 1, message = "Sức chứa phòng phải tối thiểu là 1 người")
    @Schema(description = "Sức chứa tối đa", example = "15")
    private int capacity;

    @Size(max = 10, message = "Tầng tối đa 10 ký tự")
    @Schema(description = "Tầng / Vị trí", example = "3F")
    private String floor;

    @Schema(description = "Danh sách ID các tiện ích trang bị cho phòng")
    private List<Long> equipmentIds;
}
