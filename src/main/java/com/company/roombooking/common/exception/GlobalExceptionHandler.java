package com.company.roombooking.common.exception;

import com.company.roombooking.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;
    private final ConstraintViolationTranslator constraintViolationTranslator;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = resolveMessage(ex.getErrorCode(), ex.getArgs(), ex.getMessage(), locale);

        ErrorResponse response = ErrorResponse.builder()
                .code(ex.getErrorCode().name())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .details(ex.getDetails())
                .build();

        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            details.add(error.getField() + ": " + error.getDefaultMessage());
        }

        Locale locale = LocaleContextHolder.getLocale();
        String message = resolveMessage(ErrorCode.VALIDATION_FAILED, null, ErrorCode.VALIDATION_FAILED.getDefaultMessage(), locale);

        ErrorResponse response = ErrorResponse.builder()
                .code(ErrorCode.VALIDATION_FAILED.name())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .details(details)
                .build();

        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getHttpStatus()).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
        BusinessException translated = constraintViolationTranslator.translate(ex);
        return handleBusinessException(translated, request);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockingFailureException ex, HttpServletRequest request) {
        BusinessException businessException = new BusinessException(ErrorCode.BOOKING_CONCURRENT_MODIFICATION);
        return handleBusinessException(businessException, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = resolveMessage(ErrorCode.FORBIDDEN, null, ErrorCode.FORBIDDEN.getDefaultMessage(), locale);

        ErrorResponse response = ErrorResponse.builder()
                .code(ErrorCode.FORBIDDEN.name())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .details(new ArrayList<>())
                .build();

        return ResponseEntity.status(ErrorCode.FORBIDDEN.getHttpStatus()).body(response);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = resolveMessage(ErrorCode.UNAUTHORIZED, null, ErrorCode.UNAUTHORIZED.getDefaultMessage(), locale);

        ErrorResponse response = ErrorResponse.builder()
                .code(ErrorCode.UNAUTHORIZED.name())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .details(new ArrayList<>())
                .build();

        return ResponseEntity.status(ErrorCode.UNAUTHORIZED.getHttpStatus()).body(response);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex, HttpServletRequest request) {
        Locale locale = LocaleContextHolder.getLocale();
        String message = resolveMessage(ErrorCode.INVALID_REQUEST, null, "Định dạng dữ liệu không hợp lệ", locale);

        ErrorResponse response = ErrorResponse.builder()
                .code(ErrorCode.INVALID_REQUEST.name())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .details(List.of(ex.getMessage() != null ? ex.getMessage() : "Bad request payload"))
                .build();

        return ResponseEntity.status(ErrorCode.INVALID_REQUEST.getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUncaughtException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at URI: {}", request.getRequestURI(), ex);

        Locale locale = LocaleContextHolder.getLocale();
        String message = resolveMessage(ErrorCode.INTERNAL_SERVER_ERROR, null, ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(), locale);

        ErrorResponse response = ErrorResponse.builder()
                .code(ErrorCode.INTERNAL_SERVER_ERROR.name())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .path(request.getRequestURI())
                .details(new ArrayList<>())
                .build();

        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus()).body(response);
    }

    private String resolveMessage(ErrorCode errorCode, Object[] args, String fallbackMessage, Locale locale) {
        try {
            return messageSource.getMessage(errorCode.name(), args, fallbackMessage, locale);
        } catch (Exception e) {
            return fallbackMessage != null ? fallbackMessage : errorCode.getDefaultMessage();
        }
    }
}
