package com.company.roombooking.auth.service;

import com.company.roombooking.auth.dto.ChangePasswordRequest;
import com.company.roombooking.auth.dto.LoginRequest;
import com.company.roombooking.auth.dto.LoginResponse;
import com.company.roombooking.auth.dto.RefreshTokenRequest;
import com.company.roombooking.auth.jwt.JwtService;
import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.common.util.SecurityUtils;
import com.company.roombooking.user.dto.UserResponse;
import com.company.roombooking.user.entity.User;
import com.company.roombooking.user.entity.UserStatus;
import com.company.roombooking.user.mapper.UserMapper;
import com.company.roombooking.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_LOGINS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000L; // 15 minutes

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        long now = System.currentTimeMillis();

        // Check if user is locked (manual lock or temporary lockout)
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new BusinessException(ErrorCode.USER_LOCKED);
        }
        if (user.getLockedUntil() != null && user.getLockedUntil() > now) {
            throw new BusinessException(ErrorCode.USER_LOCKED);
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            int newFailedCount = user.getFailedLoginCount() + 1;
            user.setFailedLoginCount(newFailedCount);
            if (newFailedCount >= MAX_FAILED_LOGINS) {
                user.setLockedUntil(now + LOCKOUT_DURATION_MS);
                log.warn("User {} temporarily locked out due to {} failed attempts", email, newFailedCount);
            }
            userRepository.save(user);
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // Login success - reset failed attempts and lockout
        if (user.getFailedLoginCount() > 0 || user.getLockedUntil() != null) {
            user.setFailedLoginCount(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTtlMillis())
                .mustChangePassword(user.isMustChangePassword())
                .user(userMapper.toSummaryResponse(user))
                .build();
    }

    @Transactional(readOnly = true)
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String token = request.getRefreshToken();
        if (!jwtService.validateToken(token)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String tokenType = jwtService.extractTokenType(token);
        if (!"REFRESH".equals(tokenType)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtService.extractUserId(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        long now = System.currentTimeMillis();
        if (user.getStatus() == UserStatus.LOCKED || (user.getLockedUntil() != null && user.getLockedUntil() > now)) {
            throw new BusinessException(ErrorCode.USER_LOCKED);
        }

        String newAccessToken = jwtService.generateAccessToken(user);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTtlMillis())
                .mustChangePassword(user.isMustChangePassword())
                .user(userMapper.toSummaryResponse(user))
                .build();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS, "Mật khẩu hiện tại không chính xác");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }
}
