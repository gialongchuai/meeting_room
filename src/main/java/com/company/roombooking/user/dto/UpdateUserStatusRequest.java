package com.company.roombooking.user.dto;

import com.company.roombooking.user.entity.UserStatus;
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
@Schema(description = "Yêu cầu thay đổi trạng thái tài khoản")
public class UpdateUserStatusRequest {

    @NotNull(message = "Trạng thái không được để trống")
    @Schema(description = "Trạng thái mới", example = "LOCKED")
    private UserStatus status;
}
