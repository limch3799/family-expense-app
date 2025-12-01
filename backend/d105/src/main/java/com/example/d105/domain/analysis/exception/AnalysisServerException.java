package com.example.d105.domain.analysis.exception;

import com.example.d105.common.exception.server.ServerException;

/**
 * 분석 도메인 서버 예외
 */
public class AnalysisServerException extends ServerException {
    
    public AnalysisServerException(AnalysisServerError error) {
        super(error);
    }
    
    public AnalysisServerException(AnalysisServerError error, Throwable cause) {
        super(error, cause);
    }
    
    public AnalysisServerException(AnalysisServerError error, Object... args) {
        super(error, args);
    }
    
    public AnalysisServerException(AnalysisServerError error, Throwable cause, Object... args) {
        super(error, cause, args);
    }
    
    // ============================================
    // 정적 팩토리 메소드들
    // ============================================
    
    public static AnalysisServerException dataNotFound() {
        return new AnalysisServerException(AnalysisServerError.ANALYSIS_DATA_NOT_FOUND);
    }
    
    public static AnalysisServerException insufficientData() {
        return new AnalysisServerException(AnalysisServerError.INSUFFICIENT_DATA_FOR_ANALYSIS);
    }
    
    public static AnalysisServerException groupAnalysisFailed(Throwable cause) {
        return new AnalysisServerException(AnalysisServerError.GROUP_ANALYSIS_FAILED, cause);
    }
    
    public static AnalysisServerException memberAnalysisFailed(Throwable cause) {
        return new AnalysisServerException(AnalysisServerError.MEMBER_ANALYSIS_FAILED, cause);
    }
    
    public static AnalysisServerException categoryAnalysisFailed(Throwable cause) {
        return new AnalysisServerException(AnalysisServerError.CATEGORY_ANALYSIS_FAILED, cause);
    }
    
    public static AnalysisServerException expenseCalculationFailed(Throwable cause) {
        return new AnalysisServerException(AnalysisServerError.EXPENSE_CALCULATION_FAILED, cause);
    }
    
    public static AnalysisServerException trendAnalysisFailed(Throwable cause) {
        return new AnalysisServerException(AnalysisServerError.TREND_ANALYSIS_FAILED, cause);
    }
    
    public static AnalysisServerException dailySummaryFailed(Throwable cause) {
        return new AnalysisServerException(AnalysisServerError.DAILY_SUMMARY_FAILED, cause);
    }
    
    public static AnalysisServerException monthlySummaryFailed(Throwable cause) {
        return new AnalysisServerException(AnalysisServerError.MONTHLY_SUMMARY_FAILED, cause);
    }
    
    public static AnalysisServerException calendarDataGenerationFailed(Throwable cause) {
        return new AnalysisServerException(AnalysisServerError.CALENDAR_DATA_GENERATION_FAILED, cause);
    }
    
    public static AnalysisServerException aggregationTableAccessFailed(Throwable cause) {
        return new AnalysisServerException(AnalysisServerError.AGGREGATION_TABLE_ACCESS_FAILED, cause);
    }
    
    public static AnalysisServerException readReplicaError(Throwable cause) {
        return new AnalysisServerException(AnalysisServerError.READ_REPLICA_ERROR, cause);
    }
    
    public static AnalysisServerException databaseError(Throwable cause) {
        return new AnalysisServerException(AnalysisServerError.DATABASE_ERROR, cause);
    }

    public static AnalysisServerException groupNotFound(Throwable cause) {
        return new AnalysisServerException(AnalysisServerError.GROUP_NOT_FOUND, cause);
    }
}
