package com.example.d105.ssafy.exception;

import com.example.d105.common.exception.server.ServerErrorCode;
import org.springframework.http.HttpStatus;

/**
 * SSAFY API 서버 오류 코드
 */
public enum SsafyServerError implements ServerErrorCode {
    
    // 연결 관련 오류
    CONNECTION_FAILED("SSAFY_S001", "SSAFY API 서버 연결에 실패했습니다", HttpStatus.BAD_GATEWAY),
    TIMEOUT_ERROR("SSAFY_S002", "SSAFY API 호출 시간이 초과되었습니다", HttpStatus.GATEWAY_TIMEOUT),
    
    // 인증 관련 오류
    AUTHENTICATION_FAILED("SSAFY_S003", "SSAFY API 인증에 실패했습니다", HttpStatus.BAD_GATEWAY),
    API_KEY_INVALID("SSAFY_S004", "SSAFY API 키가 유효하지 않습니다", HttpStatus.BAD_GATEWAY),
    
    // API 호출 제한
    RATE_LIMIT_EXCEEDED("SSAFY_S005", "SSAFY API 호출 한도를 초과했습니다", HttpStatus.TOO_MANY_REQUESTS),
    
    // 계좌 관련 API 오류
    ACCOUNT_API_ERROR("SSAFY_S006", "SSAFY 계좌 API 호출에 실패했습니다", HttpStatus.BAD_GATEWAY),
    CARD_API_ERROR("SSAFY_S007", "SSAFY 카드 API 호출에 실패했습니다", HttpStatus.BAD_GATEWAY),
    TRANSACTION_API_ERROR("SSAFY_S008", "SSAFY 거래내역 API 호출에 실패했습니다", HttpStatus.BAD_GATEWAY),
    
    // 적금 관련 API 오류
    SAVING_API_ERROR("SSAFY_S009", "SSAFY 적금 API 호출에 실패했습니다", HttpStatus.BAD_GATEWAY),
    DEMAND_DEPOSIT_API_ERROR("SSAFY_S010", "SSAFY 수시입출금 API 호출에 실패했습니다", HttpStatus.BAD_GATEWAY),
    
    // 응답 처리 오류
    RESPONSE_PARSING_ERROR("SSAFY_S011", "SSAFY API 응답 파싱에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_RESPONSE_FORMAT("SSAFY_S012", "SSAFY API 응답 형식이 올바르지 않습니다", HttpStatus.BAD_GATEWAY);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    
    SsafyServerError(String code, String message, HttpStatus httpStatus) {
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
