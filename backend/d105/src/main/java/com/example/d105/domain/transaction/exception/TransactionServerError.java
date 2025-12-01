package com.example.d105.domain.transaction.exception;

import com.example.d105.common.exception.server.ServerErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 거래 도메인 서버 오류 코드
 */
public enum TransactionServerError implements ServerErrorCode {
    
    // 거래내역 조회/처리 관련
    TRANSACTION_NOT_FOUND("TXN_S001", "거래내역을 찾을 수 없습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    TRANSACTION_SYNC_FAILED("TXN_S002", "거래내역 동기화에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    TRANSACTION_SAVE_FAILED("TXN_S003", "거래내역 저장에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 집계/배치 처리 관련
    AGGREGATION_FAILED("TXN_S004", "거래 집계 처리에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    BATCH_PROCESSING_FAILED("TXN_S005", "배치 처리에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    CACHE_UPDATE_FAILED("TXN_S006", "캐시 업데이트에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 외부 API 관련
    SSAFY_TRANSACTION_API_ERROR("TXN_S007", "SSAFY 거래내역 API 호출에 실패했습니다", HttpStatus.BAD_GATEWAY),
    EXTERNAL_API_TIMEOUT("TXN_S008", "외부 API 호출 시간이 초과되었습니다", HttpStatus.GATEWAY_TIMEOUT),
    
    // 카테고리 관련
    CATEGORY_NOT_FOUND("TXN_S009", "카테고리를 찾을 수 없습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    CATEGORY_UPDATE_FAILED("TXN_S010", "카테고리 변경에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 데이터베이스 관련
    DATABASE_ERROR("TXN_S011", "거래 데이터 처리 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    
    TransactionServerError(String code, String message, HttpStatus httpStatus) {
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
