package com.company.roombooking.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Yêu cầu cập nhật tiện ích thiết bị")
public class UpdateEquipmentRequest {

    @NotBlank(message = "Tên tiếng Việt không được để trống")
    @Size(max = 100, message = "Tên tiếng Việt tối đa 100 ký tự")
    @Schema(description = "Tên tiếng Việt", example = "Máy chiếu 4K")
    private String nameVi;

    @NotBlank(message = "Tên tiếng Anh không được để trống")
    @Size(max = 100, message = "Tên tiếng Anh tối đa 100 ký tự")
    @Schema(description = "Tên tiếng Anh", example = "4K Projector")
    private String nameEn;
}
