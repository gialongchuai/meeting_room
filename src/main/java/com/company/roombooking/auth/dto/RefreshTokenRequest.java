package com.company.roombooking.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
@Schema(description = "Yêu cầu làm mới Access Token")
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token không được để trống")
    @Schema(description = "JWT Refresh Token")
    private String refreshToken;
}
