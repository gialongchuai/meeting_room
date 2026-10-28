package com.company.roombooking.user.dto;

import com.company.roombooking.user.entity.UserRole;
import com.company.roombooking.user.entity.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Schema(description = "Yêu cầu tìm kiếm và phân trang người dùng")
public class UserLookupRequest {

    @Schema(description = "Từ khóa tìm kiếm theo email hoặc họ tên", example = "Nguyễn")
    private String keyword;

    @Schema(description = "Lọc theo vai trò", example = "EMPLOYEE")
    private UserRole role;

    @Schema(description = "Lọc theo trạng thái", example = "ACTIVE")
    private UserStatus status;

    @Min(value = 0, message = "Chỉ số trang phải >= 0")
    @Builder.Default
    @Schema(description = "Chỉ số trang (bắt đầu từ 0)", example = "0")
    private int page = 0;

    @Min(value = 1, message = "Kích thước trang phải >= 1")
    @Max(value = 100, message = "Kích thước trang tối đa là 100")
    @Builder.Default
    @Schema(description = "Số bản ghi trên mỗi trang (tối đa 100)", example = "10")
    private int size = 10;

    @Builder.Default
    @Schema(description = "Trường sắp xếp (userId, email, fullName, department, role, status, createdAt)", example = "createdAt")
    private String sortBy = "createdAt";

    @Builder.Default
    @Schema(description = "Hướng sắp xếp (ASC hoặc DESC)", example = "DESC")
    private String sortDir = "DESC";
}
