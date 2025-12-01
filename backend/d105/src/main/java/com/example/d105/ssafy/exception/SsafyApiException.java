package com.example.d105.ssafy.exception;

public class SsafyApiException  extends RuntimeException{

    private final String errorCode;
    private final String errorMessage;

    public SsafyApiException(String errorCode, String errorMessage){
        super(errorMessage);

        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorCode() {return errorCode ;}
    public String getErrorMessage() {return errorMessage ;}
}
