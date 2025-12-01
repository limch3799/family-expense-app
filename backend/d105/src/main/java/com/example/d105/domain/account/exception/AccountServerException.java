package com.example.d105.domain.account.exception;

import com.example.d105.common.exception.server.ServerException;

/**
 * 계좌 도메인 서버 예외
 * AccountServerError를 사용하여 5xx 서버 오류를 처리
 */
public class AccountServerException extends ServerException {
    
    public AccountServerException(AccountServerError error) {
        super(error);
    }
    
    public AccountServerException(AccountServerError error, Throwable cause) {
        super(error, cause);
    }
    
    public AccountServerException(AccountServerError error, Object... args) {
        super(error, args);
    }
    
    public AccountServerException(AccountServerError error, Throwable cause, Object... args) {
        super(error, cause, args);
    }
    
    // ============================================
    // 정적 팩토리 메소드들 (편의성 제공)
    // ============================================
    
    /**
     * 계좌를 찾을 수 없는 경우
     */
    public static AccountServerException accountNotFound() {
        return new AccountServerException(AccountServerError.ACCOUNT_NOT_FOUND);
    }
    
    public static AccountServerException accountNotFound(String accountNo) {
        return new AccountServerException(AccountServerError.ACCOUNT_NOT_FOUND, accountNo);
    }
    
    public static AccountServerException accountNotFound(Long accountId) {
        return new AccountServerException(AccountServerError.ACCOUNT_NOT_FOUND, accountId);
    }
    
    /**
     * 계좌 연결 관련 오류
     */
    public static AccountServerException connectFailed(Throwable cause) {
        return new AccountServerException(AccountServerError.ACCOUNT_CONNECT_FAILED, cause);
    }
    
    public static AccountServerException disconnectFailed(Throwable cause) {
        return new AccountServerException(AccountServerError.ACCOUNT_DISCONNECT_FAILED, cause);
    }
    
    public static AccountServerException alreadyConnected(String accountNo) {
        return new AccountServerException(AccountServerError.ACCOUNT_ALREADY_CONNECTED, accountNo);
    }
    
    public static AccountServerException notConnected(String accountNo) {
        return new AccountServerException(AccountServerError.ACCOUNT_NOT_CONNECTED, accountNo);
    }
    
    /**
     * 카드 관련 오류
     */
    public static AccountServerException cardNotFound() {
        return new AccountServerException(AccountServerError.CARD_NOT_FOUND);
    }
    
    public static AccountServerException cardNotFound(String cardNo) {
        return new AccountServerException(AccountServerError.CARD_NOT_FOUND, cardNo);
    }
    
    public static AccountServerException cardNotFound(Long cardId) {
        return new AccountServerException(AccountServerError.CARD_NOT_FOUND, cardId);
    }
    
    public static AccountServerException cardConnectFailed(Throwable cause) {
        return new AccountServerException(AccountServerError.CARD_CONNECT_FAILED, cause);
    }
    
    public static AccountServerException cardAlreadyConnected(String cardNo) {
        return new AccountServerException(AccountServerError.CARD_ALREADY_CONNECTED, cardNo);
    }
    
    /**
     * SSAFY API 관련 오류
     */
    public static AccountServerException ssafyAccountApiError(Throwable cause) {
        return new AccountServerException(AccountServerError.SSAFY_ACCOUNT_API_ERROR, cause);
    }
    
    public static AccountServerException ssafyCardApiError(Throwable cause) {
        return new AccountServerException(AccountServerError.SSAFY_CARD_API_ERROR, cause);
    }
    
    /**
     * 데이터베이스 관련 오류
     */
    public static AccountServerException databaseError(Throwable cause) {
        return new AccountServerException(AccountServerError.DATABASE_ERROR, cause);
    }
    
    /**
     * 암호화/복호화 관련 오류
     */
    public static AccountServerException encryptionError(Throwable cause) {
        return new AccountServerException(AccountServerError.ENCRYPTION_ERROR, cause);
    }
    
    public static AccountServerException decryptionError(Throwable cause) {
        return new AccountServerException(AccountServerError.DECRYPTION_ERROR, cause);
    }
}
