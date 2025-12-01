package com.example.d105.domain.account.exception;

import com.example.d105.common.exception.server.ServerErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 계좌 도메인 서버 오류 코드
 * 계좌 관련 5xx 서버 오류들을 정의
 */
public enum AccountServerError implements ServerErrorCode {
    
    // 계좌 조회 관련 오류
    ACCOUNT_NOT_FOUND("ACC_S001", "계좌를 찾을 수 없습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_LOAD_FAILED("ACC_S002", "계좌 정보 로딩에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 계좌 연결/해제 관련 오류
    ACCOUNT_CONNECT_FAILED("ACC_S003", "계좌 연결에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_DISCONNECT_FAILED("ACC_S004", "계좌 연결 해제에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_ALREADY_CONNECTED("ACC_S005", "이미 연결된 계좌입니다", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_NOT_CONNECTED("ACC_S006", "연결되지 않은 계좌입니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 카드 관련 오류
    CARD_NOT_FOUND("ACC_S007", "카드를 찾을 수 없습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    CARD_CONNECT_FAILED("ACC_S008", "카드 연결에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    CARD_DISCONNECT_FAILED("ACC_S009", "카드 연결 해제에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    CARD_ALREADY_CONNECTED("ACC_S010", "이미 연결된 카드입니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 외부 API 관련 오류
    SSAFY_ACCOUNT_API_ERROR("ACC_S011", "SSAFY 계좌 API 호출에 실패했습니다", HttpStatus.BAD_GATEWAY),
    SSAFY_CARD_API_ERROR("ACC_S012", "SSAFY 카드 API 호출에 실패했습니다", HttpStatus.BAD_GATEWAY),
    
    // 데이터베이스 관련 오류
    DATABASE_ERROR("ACC_S013", "계좌 정보 저장 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 암호화/복호화 관련 오류
    ENCRYPTION_ERROR("ACC_S014", "계좌 정보 암호화에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    DECRYPTION_ERROR("ACC_S015", "계좌 정보 복호화에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    
    AccountServerError(String code, String message, HttpStatus httpStatus) {
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
