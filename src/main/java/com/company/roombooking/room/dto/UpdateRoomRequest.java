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
@Schema(description = "Yêu cầu cập nhật thông tin phòng họp (ADMIN)")
public class UpdateRoomRequest {

    @NotBlank(message = "Tên phòng tiếng Việt không được để trống")
    @Size(max = 100, message = "Tên phòng tiếng Việt tối đa 100 ký tự")
    @Schema(description = "Tên phòng tiếng Việt", example = "Phòng Hà Nội (Nâng cấp)")
    private String nameVi;

    @NotBlank(message = "Tên phòng tiếng Anh không được để trống")
    @Size(max = 100, message = "Tên phòng tiếng Anh tối đa 100 ký tự")
    @Schema(description = "Tên phòng tiếng Anh", example = "Hanoi Room (Upgraded)")
    private String nameEn;

    @Size(max = 500, message = "Mô tả tiếng Việt tối đa 500 ký tự")
    @Schema(description = "Mô tả tiếng Việt", example = "Phòng họp hiện đại trang bị hội nghị truyền hình")
    private String descriptionVi;

    @Size(max = 500, message = "Mô tả tiếng Anh tối đa 500 ký tự")
    @Schema(description = "Mô tả tiếng Anh", example = "Modern meeting room equipped with video conference")
    private String descriptionEn;

    @Min(value = 1, message = "Sức chứa phòng phải tối thiểu là 1 người")
    @Schema(description = "Sức chứa tối đa", example = "20")
    private int capacity;

    @Size(max = 10, message = "Tầng tối đa 10 ký tự")
    @Schema(description = "Tầng / Vị trí", example = "3F")
    private String floor;

    @Schema(description = "Danh sách ID các tiện ích trang bị cho phòng")
    private List<Long> equipmentIds;
}
