package com.company.roombooking.user.dto;

import com.company.roombooking.user.entity.UserRole;
import com.company.roombooking.user.entity.UserStatus;
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
@Schema(description = "Thông tin cơ bản người dùng")
public class UserResponse {

    @Schema(description = "ID người dùng", example = "1")
    private Long userId;

    @Schema(description = "Email người dùng", example = "employee@company.com")
    private String email;

    @Schema(description = "Họ và tên", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Phòng ban", example = "Kỹ thuật")
    private String department;

    @Schema(description = "Vai trò", example = "EMPLOYEE")
    private UserRole role;

    @Schema(description = "Trạng thái tài khoản", example = "ACTIVE")
    private UserStatus status;
}
