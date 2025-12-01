package com.example.d105.domain.report.exception;

import lombok.Getter;

@Getter
public class ReportException extends RuntimeException {

    private final String errorCode;

    public ReportException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ReportException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
