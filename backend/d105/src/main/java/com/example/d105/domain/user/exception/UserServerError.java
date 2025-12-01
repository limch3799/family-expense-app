package com.example.d105.domain.user.exception;

import com.example.d105.common.exception.server.ServerErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 사용자 도메인 서버 오류 코드
 */
public enum UserServerError implements ServerErrorCode {
    
    // 사용자 계정 관련
    USER_NOT_FOUND("USER_S001", "사용자를 찾을 수 없습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_CREATION_FAILED("USER_S002", "사용자 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_UPDATE_FAILED("USER_S003", "사용자 정보 업데이트에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 알림 관련
    FCM_TOKEN_SAVE_FAILED("USER_S004", "FCM 토큰 저장에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    NOTIFICATION_SEND_FAILED("USER_S005", "알림 전송에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 인증/보안 관련
    PASSWORD_ENCRYPTION_FAILED("USER_S006", "비밀번호 암호화에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_KEY_GENERATION_FAILED("USER_S007", "사용자 키 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 데이터베이스 관련
    DATABASE_ERROR("USER_S008", "사용자 데이터 처리 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    
    UserServerError(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
    
    @Override
    public String getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
    
    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
