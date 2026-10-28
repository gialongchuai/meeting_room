package com.company.roombooking.auth.dto;

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
@Schema(description = "Phản hồi đăng nhập")
public class LoginResponse {

    @Schema(description = "JWT Access Token")
    private String accessToken;

    @Schema(description = "JWT Refresh Token")
    private String refreshToken;

    @Schema(description = "Loại Token", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "Thời gian sống của Access Token (ms)", example = "1800000")
    private Long expiresIn;

    @Schema(description = "Bắt buộc đổi mật khẩu", example = "false")
    private boolean mustChangePassword;

    @Schema(description = "Thông tin vắn tắt người dùng")
    private UserSummaryResponse user;
}
