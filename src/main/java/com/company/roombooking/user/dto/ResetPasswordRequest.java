package com.company.roombooking.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
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
@Schema(description = "Yêu cầu đặt lại mật khẩu của quản trị viên")
public class ResetPasswordRequest {

    @Size(min = 8, message = "Mật khẩu mới phải có tối thiểu 8 ký tự")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Mật khẩu phải chứa ít nhất một chữ cái và một chữ số")
    @Schema(description = "Mật khẩu mới (nếu để trống hệ thống sẽ sinh mật khẩu ngẫu nhiên)", example = "ResetPass@123")
    private String newPassword;
}
