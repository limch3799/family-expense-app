package com.example.d105.domain.user.exception;


import lombok.Getter;

@Getter
public class UserException extends RuntimeException {
    private final String errorCode;

    public UserException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public UserException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}