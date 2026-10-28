package com.company.roombooking.user.controller;

import com.company.roombooking.common.dto.PageResponse;
import com.company.roombooking.user.dto.CreateUserRequest;
import com.company.roombooking.user.dto.ResetPasswordRequest;
import com.company.roombooking.user.dto.UpdateUserRoleRequest;
import com.company.roombooking.user.dto.UpdateUserStatusRequest;
import com.company.roombooking.user.dto.UserDetailResponse;
import com.company.roombooking.user.dto.UserLookupRequest;
import com.company.roombooking.user.dto.UserResponse;
import com.company.roombooking.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Quản trị người dùng (ADMIN)", description = "Các API quản lý tài khoản người dùng dành cho quản trị viên")
public class AdminUserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Tạo tài khoản người dùng", description = "Admin tạo tài khoản mới cho nhân viên")
    public ResponseEntity<UserDetailResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping
    @Operation(summary = "Danh sách người dùng", description = "Tìm kiếm và phân trang danh sách người dùng")
    public ResponseEntity<PageResponse<UserResponse>> getUsers(@Valid @ModelAttribute UserLookupRequest request) {
        return ResponseEntity.ok(userService.getUsers(request));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Chi tiết người dùng", description = "Xem thông tin chi tiết một tài khoản")
    public ResponseEntity<UserDetailResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PatchMapping("/{userId}/status")
    @Operation(summary = "Khóa hoặc mở khóa người dùng", description = "Thay đổi trạng thái tài khoản người dùng")
    public ResponseEntity<UserDetailResponse> updateStatus(@PathVariable Long userId,
                                                          @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(userService.updateStatus(userId, request));
    }

    @PatchMapping("/{userId}/role")
    @Operation(summary = "Đổi vai trò người dùng", description = "Nâng hoặc hạ quyền vai trò người dùng")
    public ResponseEntity<UserDetailResponse> updateRole(@PathVariable Long userId,
                                                        @Valid @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(userService.updateRole(userId, request));
    }

    @PostMapping("/{userId}/reset-password")
    @Operation(summary = "Đặt lại mật khẩu", description = "Admin đặt lại mật khẩu cho tài khoản")
    public ResponseEntity<UserDetailResponse> resetPassword(@PathVariable Long userId,
                                                            @RequestBody(required = false) ResetPasswordRequest request) {
        return ResponseEntity.ok(userService.resetPassword(userId, request));
    }
}
