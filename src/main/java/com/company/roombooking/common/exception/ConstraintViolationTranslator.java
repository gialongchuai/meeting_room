package com.company.roombooking.common.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class ConstraintViolationTranslator {

    public static final String CONSTRAINT_ROOM_OVERLAP = "ex_booking_no_overlap";
    public static final String CONSTRAINT_USER_OVERLAP = "ex_booking_user_no_overlap";

    /**
     * Translates a DataIntegrityViolationException to a domain BusinessException
     * if the root cause matches known PostgreSQL exclusion or unique constraints.
     */
    public BusinessException translate(DataIntegrityViolationException ex) {
        String constraintName = extractConstraintName(ex);
        String fullMessage = extractFullMessage(ex);

        if (matchesConstraint(constraintName, fullMessage, CONSTRAINT_ROOM_OVERLAP)) {
            return new BusinessException(ErrorCode.ROOM_ALREADY_BOOKED);
        }
        if (matchesConstraint(constraintName, fullMessage, CONSTRAINT_USER_OVERLAP)) {
            return new BusinessException(ErrorCode.USER_DOUBLE_BOOKED);
        }
        if (matchesConstraint(constraintName, fullMessage, "app_user_email_key")
                || (fullMessage.contains("app_user") && fullMessage.contains("email"))) {
            return new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (matchesConstraint(constraintName, fullMessage, "room_code_key")
                || (fullMessage.contains("room") && fullMessage.contains("code"))) {
            return new BusinessException(ErrorCode.ROOM_CODE_ALREADY_EXISTS);
        }
        if (matchesConstraint(constraintName, fullMessage, "equipment_code_key")
                || (fullMessage.contains("equipment") && fullMessage.contains("code"))) {
            return new BusinessException(ErrorCode.EQUIPMENT_CODE_ALREADY_EXISTS);
        }

        // Unknown constraint violation
        return new BusinessException(ErrorCode.INVALID_REQUEST, "Vi phạm ràng buộc dữ liệu toàn vẹn");
    }

    private boolean matchesConstraint(String constraintName, String fullMessage, String targetConstraint) {
        if (constraintName != null && constraintName.equalsIgnoreCase(targetConstraint)) {
            return true;
        }
        return fullMessage != null && fullMessage.toLowerCase().contains(targetConstraint.toLowerCase());
    }

    private String extractConstraintName(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof org.hibernate.exception.ConstraintViolationException cve) {
                if (cve.getConstraintName() != null) {
                    return cve.getConstraintName();
                }
            }
            current = current.getCause();
        }
        return null;
    }

    private String extractFullMessage(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        Throwable current = ex;
        while (current != null) {
            if (current.getMessage() != null) {
                sb.append(current.getMessage()).append(" ");
            }
            current = current.getCause();
        }
        return sb.toString();
    }
}
