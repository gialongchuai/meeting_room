package com.company.roombooking.user.controller;

import com.company.roombooking.auth.service.AuthService;
import com.company.roombooking.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Người dùng", description = "Các API thông tin cá nhân của người dùng")
public class UserController {

    private final AuthService authService;

    @GetMapping("/me")
    @Operation(summary = "Xem thông tin cá nhân", description = "Lấy thông tin của người dùng đang đăng nhập")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }
}
