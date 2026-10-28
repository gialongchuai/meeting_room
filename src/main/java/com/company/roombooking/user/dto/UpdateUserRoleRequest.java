package com.company.roombooking.user.dto;

import com.company.roombooking.user.entity.UserRole;
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
@Schema(description = "Yêu cầu thay đổi vai trò người dùng")
public class UpdateUserRoleRequest {

    @NotNull(message = "Vai trò không được để trống")
    @Schema(description = "Vai trò mới", example = "ADMIN")
    private UserRole role;
}
