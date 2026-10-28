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
@Schema(description = "Thông tin chi tiết người dùng")
public class UserDetailResponse {

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

    @Schema(description = "Yêu cầu đổi mật khẩu", example = "false")
    private boolean mustChangePassword;

    @Schema(description = "Số lần đăng nhập sai", example = "0")
    private int failedLoginCount;

    @Schema(description = "Thời điểm hết hạn khóa tạm (epoch ms UTC)", example = "1788321600000")
    private Long lockedUntil;

    @Schema(description = "Thời điểm tạo (epoch ms UTC)", example = "1704067200000")
    private Long createdAt;

    @Schema(description = "Thời điểm cập nhật (epoch ms UTC)", example = "1704067200000")
    private Long updatedAt;
}
