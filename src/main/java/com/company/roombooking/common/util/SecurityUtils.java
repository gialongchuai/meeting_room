package com.company.roombooking.common.util;

import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    public static Long getCurrentUserId() {
        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .map(auth -> {
                    if (auth.getPrincipal() instanceof AuthenticatedUser user) {
                        return user.getUserId();
                    }
                    return null;
                })
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    public static String getCurrentUserEmail() {
        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
    }

    public static boolean isCurrentUserAdmin() {
        return getAuthentication()
                .filter(Authentication::isAuthenticated)
                .map(auth -> auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
                .orElse(false);
    }
}
