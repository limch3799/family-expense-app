package com.example.d105.domain.transaction.exception;

import com.example.d105.common.exception.server.ServerException;

/**
 * 거래 도메인 서버 예외
 */
public class TransactionServerException extends ServerException {
    
    public TransactionServerException(TransactionServerError error) {
        super(error);
    }
    
    public TransactionServerException(TransactionServerError error, Throwable cause) {
        super(error, cause);
    }
    
    public TransactionServerException(TransactionServerError error, Object... args) {
        super(error, args);
    }
    
    public TransactionServerException(TransactionServerError error, Throwable cause, Object... args) {
        super(error, cause, args);
    }
    
    // ============================================
    // 정적 팩토리 메소드들
    // ============================================
    
    // 거래내역 관련
    public static TransactionServerException transactionNotFound() {
        return new TransactionServerException(TransactionServerError.TRANSACTION_NOT_FOUND);
    }
    
    public static TransactionServerException transactionNotFound(Long transactionId) {
        return new TransactionServerException(TransactionServerError.TRANSACTION_NOT_FOUND, transactionId);
    }
    
    public static TransactionServerException syncFailed(Throwable cause) {
        return new TransactionServerException(TransactionServerError.TRANSACTION_SYNC_FAILED, cause);
    }
    
    public static TransactionServerException saveFailed(Throwable cause) {
        return new TransactionServerException(TransactionServerError.TRANSACTION_SAVE_FAILED, cause);
    }
    
    // 집계/배치 관련
    public static TransactionServerException aggregationFailed(Throwable cause) {
        return new TransactionServerException(TransactionServerError.AGGREGATION_FAILED, cause);
    }
    
    public static TransactionServerException batchProcessingFailed(Throwable cause) {
        return new TransactionServerException(TransactionServerError.BATCH_PROCESSING_FAILED, cause);
    }
    
    public static TransactionServerException cacheUpdateFailed(Throwable cause) {
        return new TransactionServerException(TransactionServerError.CACHE_UPDATE_FAILED, cause);
    }
    
    // 외부 API 관련
    public static TransactionServerException ssafyApiError(Throwable cause) {
        return new TransactionServerException(TransactionServerError.SSAFY_TRANSACTION_API_ERROR, cause);
    }
    
    public static TransactionServerException externalApiTimeout(String apiName) {
        return new TransactionServerException(TransactionServerError.EXTERNAL_API_TIMEOUT, apiName);
    }
    
    // 카테고리 관련
    public static TransactionServerException categoryNotFound() {
        return new TransactionServerException(TransactionServerError.CATEGORY_NOT_FOUND);
    }
    
    public static TransactionServerException categoryUpdateFailed(Throwable cause) {
        return new TransactionServerException(TransactionServerError.CATEGORY_UPDATE_FAILED, cause);
    }
    
    // 데이터베이스 관련
    public static TransactionServerException databaseError(Throwable cause) {
        return new TransactionServerException(TransactionServerError.DATABASE_ERROR, cause);
    }
}
