package com.example.d105.domain.group.exception;


import lombok.Getter;

@Getter
public class GroupException extends RuntimeException {
    private final String errorCode;

    public GroupException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public GroupException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}