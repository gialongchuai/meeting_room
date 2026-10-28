package com.company.roombooking.auth.jwt;

import com.company.roombooking.common.dto.ErrorResponse;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.common.util.AuthenticatedUser;
import com.company.roombooking.user.entity.User;
import com.company.roombooking.user.entity.UserStatus;
import com.company.roombooking.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        if (!jwtService.validateToken(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        String tokenType = jwtService.extractTokenType(jwt);
        if (!"ACCESS".equals(tokenType)) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = jwtService.extractUserId(jwt);
        if (userId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        User user = userOpt.get();
        long now = System.currentTimeMillis();

        // Check if user is locked (manual lock or temporary lockout)
        if (user.getStatus() == UserStatus.LOCKED || (user.getLockedUntil() != null && user.getLockedUntil() > now)) {
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.USER_LOCKED,
                    "Tài khoản đã bị khóa", request.getRequestURI());
            return;
        }

        // Check must_change_password enforcement
        String path = request.getRequestURI();
        if (user.isMustChangePassword()) {
            boolean isAllowedPath = path.equals("/api/v1/auth/change-password") || path.equals("/api/v1/users/me");
            if (!isAllowedPath) {
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.PASSWORD_CHANGE_REQUIRED,
                        "Bắt buộc phải đổi mật khẩu trước khi tiếp tục", path);
                return;
            }
        }

        AuthenticatedUser principal = AuthenticatedUser.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .mustChangePassword(user.isMustChangePassword())
                .build();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, ErrorCode errorCode,
                                   String message, String path) throws IOException {
        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(errorCode.name())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .path(path)
                .details(new ArrayList<>())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
