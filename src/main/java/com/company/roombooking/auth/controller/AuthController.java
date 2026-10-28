package com.company.roombooking.auth.controller;

import com.company.roombooking.auth.dto.ChangePasswordRequest;
import com.company.roombooking.auth.dto.LoginRequest;
import com.company.roombooking.auth.dto.LoginResponse;
import com.company.roombooking.auth.dto.RefreshTokenRequest;
import com.company.roombooking.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Xác thực & Tài khoản", description = "Các API đăng nhập, làm mới token và đổi mật khẩu")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Đăng nhập hệ thống", description = "Trả về JWT Access Token và Refresh Token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Làm mới Access Token", description = "Đổi Refresh Token lấy Access Token mới")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Đổi mật khẩu", description = "Đổi mật khẩu tài khoản hiện tại")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }
}
