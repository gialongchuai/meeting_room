package com.company.roombooking.user.dto;

import com.company.roombooking.user.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Yêu cầu tạo tài khoản người dùng mới (ADMIN)")
public class CreateUserRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Schema(description = "Email người dùng", example = "nhanvien@company.com")
    private String email;

    @NotBlank(message = "Họ và tên không được để trống")
    @Schema(description = "Họ và tên", example = "Trần Thị B")
    private String fullName;

    @Schema(description = "Phòng ban", example = "Kỹ thuật")
    private String department;

    @NotNull(message = "Vai trò không được để trống")
    @Schema(description = "Vai trò người dùng", example = "EMPLOYEE")
    private UserRole role;

    @Size(min = 8, message = "Mật khẩu phải có tối thiểu 8 ký tự")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "Mật khẩu phải chứa ít nhất một chữ cái và một chữ số")
    @Schema(description = "Mật khẩu khởi tạo (nếu để trống hệ thống sẽ tạo mật khẩu mặc định)", example = "InitPass@123")
    private String initialPassword;
}
