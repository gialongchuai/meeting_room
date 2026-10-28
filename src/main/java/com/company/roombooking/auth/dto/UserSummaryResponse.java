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
@Schema(description = "Thông tin tóm tắt người dùng")
public class UserSummaryResponse {

    @Schema(description = "ID người dùng", example = "1")
    private Long userId;

    @Schema(description = "Email người dùng", example = "employee@company.com")
    private String email;

    @Schema(description = "Họ và tên", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Vai trò", example = "EMPLOYEE")
    private String role;
}
