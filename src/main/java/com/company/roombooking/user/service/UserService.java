package com.company.roombooking.user.service;

import com.company.roombooking.common.dto.PageResponse;
import com.company.roombooking.common.exception.BusinessException;
import com.company.roombooking.common.exception.ErrorCode;
import com.company.roombooking.common.util.SecurityUtils;
import com.company.roombooking.user.dto.CreateUserRequest;
import com.company.roombooking.user.dto.ResetPasswordRequest;
import com.company.roombooking.user.dto.UpdateUserRoleRequest;
import com.company.roombooking.user.dto.UpdateUserStatusRequest;
import com.company.roombooking.user.dto.UserDetailResponse;
import com.company.roombooking.user.dto.UserLookupRequest;
import com.company.roombooking.user.dto.UserResponse;
import com.company.roombooking.user.entity.User;
import com.company.roombooking.user.entity.UserRole;
import com.company.roombooking.user.entity.UserStatus;
import com.company.roombooking.user.mapper.UserMapper;
import com.company.roombooking.user.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "userId", "email", "fullName", "department", "role", "status", "createdAt"
    );

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private void ensureAdmin() {
        if (!SecurityUtils.isCurrentUserAdmin()) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    @Transactional
    public UserDetailResponse createUser(CreateUserRequest request) {
        ensureAdmin();

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String initialPassword = StringUtils.hasText(request.getInitialPassword())
                ? request.getInitialPassword()
                : "User@123456";

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(initialPassword))
                .fullName(request.getFullName().trim())
                .department(request.getDepartment() != null ? request.getDepartment().trim() : null)
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .mustChangePassword(true)
                .failedLoginCount(0)
                .lockedUntil(null)
                .build();

        User saved = userRepository.save(user);
        return userMapper.toDetailResponse(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(UserLookupRequest request) {
        ensureAdmin();

        int page = Math.max(0, request.getPage());
        int size = Math.min(100, Math.max(1, request.getSize()));

        String sortBy = request.getSortBy();
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "createdAt";
        }

        Sort.Direction direction = "ASC".equalsIgnoreCase(request.getSortDir())
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getKeyword())) {
                String keyword = "%" + request.getKeyword().trim().toLowerCase() + "%";
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), keyword);
                Predicate namePredicate = cb.like(cb.lower(root.get("fullName")), keyword);
                predicates.add(cb.or(emailPredicate, namePredicate));
            }

            if (request.getRole() != null) {
                predicates.add(cb.equal(root.get("role"), request.getRole()));
            }

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> userPage = userRepository.findAll(spec, pageable);
        return PageResponse.of(userPage.map(userMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUserById(Long userId) {
        ensureAdmin();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toDetailResponse(user);
    }

    @Transactional
    public UserDetailResponse updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        ensureAdmin();

        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_SELF, "Admin không thể tự khóa tài khoản của chính mình");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setStatus(request.getStatus());
        if (request.getStatus() == UserStatus.ACTIVE) {
            user.setFailedLoginCount(0);
            user.setLockedUntil(null);
        }

        User updated = userRepository.save(user);
        return userMapper.toDetailResponse(updated);
    }

    @Transactional
    public UserDetailResponse updateUserRole(Long userId, UpdateUserRoleRequest request) {
        ensureAdmin();

        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_SELF, "Admin không thể tự hạ quyền của chính mình");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setRole(request.getRole());
        User updated = userRepository.save(user);
        return userMapper.toDetailResponse(updated);
    }

    @Transactional
    public UserDetailResponse resetPassword(Long userId, ResetPasswordRequest request) {
        ensureAdmin();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String newPassword = (request != null && StringUtils.hasText(request.getNewPassword()))
                ? request.getNewPassword()
                : "Reset@123456";

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(true);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);

        User updated = userRepository.save(user);
        return userMapper.toDetailResponse(updated);
    }
}
