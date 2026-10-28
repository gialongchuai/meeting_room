package com.company.roombooking.config;

import com.company.roombooking.user.entity.User;
import com.company.roombooking.user.entity.UserRole;
import com.company.roombooking.user.entity.UserStatus;
import com.company.roombooking.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppStartupValidator implements ApplicationRunner {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        validateSecrets();
        initDefaultAdmin();
    }

    private void validateSecrets() {
        // 1. Validate JWT_SECRET
        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException("Cấu hình bảo mật lỗi: JWT_SECRET không được để trống");
        }
        byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException("Cấu hình bảo mật lỗi: JWT_SECRET phải có độ dài tối thiểu 32 byte");
        }
        String lowerSecret = jwtSecret.toLowerCase();
        if (lowerSecret.contains("change-me") || lowerSecret.contains("placeholder") || lowerSecret.contains("your-secret")) {
            throw new IllegalStateException("Cấu hình bảo mật lỗi: JWT_SECRET đang sử dụng giá trị mẫu (placeholder), vui lòng thay đổi");
        }

        // 2. Validate APP_ADMIN_EMAIL
        if (!StringUtils.hasText(adminEmail) || !adminEmail.contains("@")) {
            throw new IllegalStateException("Cấu hình quản trị lỗi: APP_ADMIN_EMAIL không hợp lệ hoặc để trống");
        }
        String lowerEmail = adminEmail.toLowerCase();
        if (lowerEmail.contains("change-me") || lowerEmail.contains("placeholder")) {
            throw new IllegalStateException("Cấu hình quản trị lỗi: APP_ADMIN_EMAIL đang sử dụng giá trị mẫu (placeholder)");
        }

        // 3. Validate APP_ADMIN_PASSWORD
        if (!StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException("Cấu hình quản trị lỗi: APP_ADMIN_PASSWORD không được để trống");
        }
        if (!PASSWORD_PATTERN.matcher(adminPassword).matches()) {
            throw new IllegalStateException("Cấu hình quản trị lỗi: APP_ADMIN_PASSWORD phải có tối thiểu 8 ký tự, bao gồm cả chữ cái và số");
        }
        String lowerPass = adminPassword.toLowerCase();
        if (lowerPass.contains("change-me") || lowerPass.contains("placeholder")) {
            throw new IllegalStateException("Cấu hình quản trị lỗi: APP_ADMIN_PASSWORD đang sử dụng giá trị mẫu (placeholder), vui lòng thay đổi");
        }
    }

    private void initDefaultAdmin() {
        String email = adminEmail.trim().toLowerCase();
        if (!userRepository.existsByEmailIgnoreCase(email)) {
            User admin = User.builder()
                    .email(email)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .fullName("Quản trị viên hệ thống")
                    .department("Quản trị hệ thống")
                    .role(UserRole.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .mustChangePassword(true)
                    .failedLoginCount(0)
                    .lockedUntil(null)
                    .build();

            userRepository.save(admin);
            log.info("Khởi tạo thành công tài khoản quản trị mặc định: {}", email);
        }
    }
}
