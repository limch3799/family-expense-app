package com.example.d105.domain.user.exception;

import com.example.d105.common.exception.server.ServerException;

/**
 * 사용자 도메인 서버 예외
 */
public class UserServerException extends ServerException {
    
    public UserServerException(UserServerError error) {
        super(error);
    }
    
    public UserServerException(UserServerError error, Throwable cause) {
        super(error, cause);
    }
    
    public UserServerException(UserServerError error, Object... args) {
        super(error, args);
    }
    
    public UserServerException(UserServerError error, Throwable cause, Object... args) {
        super(error, cause, args);
    }
    
    // ============================================
    // 정적 팩토리 메소드들
    // ============================================
    
    public static UserServerException userNotFound() {
        return new UserServerException(UserServerError.USER_NOT_FOUND);
    }
    
    public static UserServerException userNotFound(String email) {
        return new UserServerException(UserServerError.USER_NOT_FOUND, email);
    }
    
    public static UserServerException creationFailed(Throwable cause) {
        return new UserServerException(UserServerError.USER_CREATION_FAILED, cause);
    }
    
    public static UserServerException updateFailed(Throwable cause) {
        return new UserServerException(UserServerError.USER_UPDATE_FAILED, cause);
    }
    
    public static UserServerException fcmTokenSaveFailed(Throwable cause) {
        return new UserServerException(UserServerError.FCM_TOKEN_SAVE_FAILED, cause);
    }
    
    public static UserServerException notificationSendFailed(Throwable cause) {
        return new UserServerException(UserServerError.NOTIFICATION_SEND_FAILED, cause);
    }
    
    public static UserServerException passwordEncryptionFailed(Throwable cause) {
        return new UserServerException(UserServerError.PASSWORD_ENCRYPTION_FAILED, cause);
    }
    
    public static UserServerException userKeyGenerationFailed(Throwable cause) {
        return new UserServerException(UserServerError.USER_KEY_GENERATION_FAILED, cause);
    }
    
    public static UserServerException databaseError(Throwable cause) {
        return new UserServerException(UserServerError.DATABASE_ERROR, cause);
    }
}
