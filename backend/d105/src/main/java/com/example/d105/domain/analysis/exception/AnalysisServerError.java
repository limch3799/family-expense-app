package com.example.d105.domain.analysis.exception;

import com.example.d105.common.exception.server.ServerErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 분석 도메인 서버 오류 코드
 */
public enum AnalysisServerError implements ServerErrorCode {
    
    // 데이터 조회 관련
    ANALYSIS_DATA_NOT_FOUND("ANALYSIS_S001", "분석할 데이터를 찾을 수 없습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    INSUFFICIENT_DATA_FOR_ANALYSIS("ANALYSIS_S002", "분석을 위한 충분한 데이터가 없습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 그룹 분석 관련
    GROUP_ANALYSIS_FAILED("ANALYSIS_S003", "그룹 분석 처리에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    MEMBER_ANALYSIS_FAILED("ANALYSIS_S004", "멤버별 분석 처리에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),

    GROUP_NOT_FOUND("ANALYSIS_S014", "그룹을 찾을 수 없습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 카테고리 분석 관련
    CATEGORY_ANALYSIS_FAILED("ANALYSIS_S005", "카테고리 분석 처리에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    EXPENSE_CALCULATION_FAILED("ANALYSIS_S006", "지출 계산 처리에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 트렌드 분석 관련
    TREND_ANALYSIS_FAILED("ANALYSIS_S007", "트렌드 분석 처리에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    DAILY_SUMMARY_FAILED("ANALYSIS_S008", "일별 요약 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    MONTHLY_SUMMARY_FAILED("ANALYSIS_S009", "월별 요약 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 캘린더 분석 관련
    CALENDAR_DATA_GENERATION_FAILED("ANALYSIS_S010", "캘린더 데이터 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 집계 테이블 관련
    AGGREGATION_TABLE_ACCESS_FAILED("ANALYSIS_S011", "집계 테이블 접근에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    READ_REPLICA_ERROR("ANALYSIS_S012", "읽기 전용 DB 접근에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 데이터베이스 관련
    DATABASE_ERROR("ANALYSIS_S013", "분석 데이터 처리 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    
    AnalysisServerError(String code, String message, HttpStatus httpStatus) {
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
