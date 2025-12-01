package com.example.d105.ssafy.exception;

import com.example.d105.common.exception.server.ServerException;

/**
 * SSAFY API 서버 예외
 */
public class SsafyServerException extends ServerException {
    
    public SsafyServerException(SsafyServerError error) {
        super(error);
    }
    
    public SsafyServerException(SsafyServerError error, Throwable cause) {
        super(error, cause);
    }
    
    public SsafyServerException(SsafyServerError error, Object... args) {
        super(error, args);
    }
    
    public SsafyServerException(SsafyServerError error, Throwable cause, Object... args) {
        super(error, cause, args);
    }
    
    // ============================================
    // 정적 팩토리 메소드들
    // ============================================
    
    public static SsafyServerException connectionFailed(Throwable cause) {
        return new SsafyServerException(SsafyServerError.CONNECTION_FAILED, cause);
    }
    
    public static SsafyServerException timeoutError(String apiName) {
        return new SsafyServerException(SsafyServerError.TIMEOUT_ERROR, apiName);
    }
    
    public static SsafyServerException authenticationFailed(Throwable cause) {
        return new SsafyServerException(SsafyServerError.AUTHENTICATION_FAILED, cause);
    }
    
    public static SsafyServerException apiKeyInvalid() {
        return new SsafyServerException(SsafyServerError.API_KEY_INVALID);
    }
    
    public static SsafyServerException rateLimitExceeded() {
        return new SsafyServerException(SsafyServerError.RATE_LIMIT_EXCEEDED);
    }
    
    public static SsafyServerException accountApiError(Throwable cause) {
        return new SsafyServerException(SsafyServerError.ACCOUNT_API_ERROR, cause);
    }
    
    public static SsafyServerException cardApiError(Throwable cause) {
        return new SsafyServerException(SsafyServerError.CARD_API_ERROR, cause);
    }
    
    public static SsafyServerException transactionApiError(Throwable cause) {
        return new SsafyServerException(SsafyServerError.TRANSACTION_API_ERROR, cause);
    }
    
    public static SsafyServerException savingApiError(Throwable cause) {
        return new SsafyServerException(SsafyServerError.SAVING_API_ERROR, cause);
    }
    
    public static SsafyServerException demandDepositApiError(Throwable cause) {
        return new SsafyServerException(SsafyServerError.DEMAND_DEPOSIT_API_ERROR, cause);
    }
    
    public static SsafyServerException responseParsingError(Throwable cause) {
        return new SsafyServerException(SsafyServerError.RESPONSE_PARSING_ERROR, cause);
    }
    
    public static SsafyServerException invalidResponseFormat(String apiName) {
        return new SsafyServerException(SsafyServerError.INVALID_RESPONSE_FORMAT, apiName);
    }
}
