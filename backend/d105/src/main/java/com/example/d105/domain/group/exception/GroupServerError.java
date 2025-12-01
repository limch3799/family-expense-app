package com.example.d105.domain.group.exception;

import com.example.d105.common.exception.server.ServerErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 그룹 도메인 서버 오류 코드
 */
public enum GroupServerError implements ServerErrorCode {
    
    // 그룹 관련
    GROUP_NOT_FOUND("GRP_S001", "그룹을 찾을 수 없습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    GROUP_CREATION_FAILED("GRP_S002", "그룹 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    GROUP_UPDATE_FAILED("GRP_S003", "그룹 정보 업데이트에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    GROUP_DELETION_FAILED("GRP_S004", "그룹 삭제에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 그룹 멤버 관련
    MEMBER_JOIN_FAILED("GRP_S005", "그룹 가입에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    MEMBER_LEAVE_FAILED("GRP_S006", "그룹 탈퇴에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    MEMBER_SYNC_FAILED("GRP_S007", "그룹 멤버 동기화에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 적금 계획 관련
    SAVING_PLAN_CREATION_FAILED("GRP_S008", "적금 계획 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    SAVING_PLAN_UPDATE_FAILED("GRP_S009", "적금 계획 업데이트에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    SAVING_PLAN_NOT_FOUND("GRP_S010", "적금 계획을 찾을 수 없습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 외부 API 관련
    SSAFY_SAVING_API_ERROR("GRP_S011", "SSAFY 적금 API 호출에 실패했습니다", HttpStatus.BAD_GATEWAY),
    
    // 데이터베이스 관련
    DATABASE_ERROR("GRP_S012", "그룹 데이터 처리 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    
    GroupServerError(String code, String message, HttpStatus httpStatus) {
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
