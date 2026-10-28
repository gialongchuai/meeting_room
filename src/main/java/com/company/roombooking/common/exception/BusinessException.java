package com.company.roombooking.common.exception;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] args;
    private final List<String> details;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.args = null;
        this.details = new ArrayList<>();
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.args = null;
        this.details = new ArrayList<>();
    }

    public BusinessException(ErrorCode errorCode, Object[] args) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.args = args;
        this.details = new ArrayList<>();
    }

    public BusinessException(ErrorCode errorCode, String customMessage, List<String> details) {
        super(customMessage);
        this.errorCode = errorCode;
        this.args = null;
        this.details = details != null ? details : new ArrayList<>();
    }
}
