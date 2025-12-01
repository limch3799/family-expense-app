package com.example.d105.domain.group.exception;

import com.example.d105.common.exception.server.ServerException;

/**
 * 그룹 도메인 서버 예외
 */
public class GroupServerException extends ServerException {
    
    public GroupServerException(GroupServerError error) {
        super(error);
    }
    
    public GroupServerException(GroupServerError error, Throwable cause) {
        super(error, cause);
    }
    
    public GroupServerException(GroupServerError error, Object... args) {
        super(error, args);
    }
    
    public GroupServerException(GroupServerError error, Throwable cause, Object... args) {
        super(error, cause, args);
    }
    
    // ============================================
    // 정적 팩토리 메소드들
    // ============================================
    
    public static GroupServerException groupNotFound() {
        return new GroupServerException(GroupServerError.GROUP_NOT_FOUND);
    }
    
    public static GroupServerException groupNotFound(Long groupId) {
        return new GroupServerException(GroupServerError.GROUP_NOT_FOUND, groupId);
    }
    
    public static GroupServerException creationFailed(Throwable cause) {
        return new GroupServerException(GroupServerError.GROUP_CREATION_FAILED, cause);
    }
    
    public static GroupServerException updateFailed(Throwable cause) {
        return new GroupServerException(GroupServerError.GROUP_UPDATE_FAILED, cause);
    }
    
    public static GroupServerException deletionFailed(Long groupId, Throwable cause) {
        return new GroupServerException(GroupServerError.GROUP_DELETION_FAILED, cause, groupId);
    }
    
    public static GroupServerException memberJoinFailed(Throwable cause) {
        return new GroupServerException(GroupServerError.MEMBER_JOIN_FAILED, cause);
    }
    
    public static GroupServerException memberLeaveFailed(Throwable cause) {
        return new GroupServerException(GroupServerError.MEMBER_LEAVE_FAILED, cause);
    }
    
    public static GroupServerException savingPlanCreationFailed(Throwable cause) {
        return new GroupServerException(GroupServerError.SAVING_PLAN_CREATION_FAILED, cause);
    }
    
    public static GroupServerException savingPlanNotFound() {
        return new GroupServerException(GroupServerError.SAVING_PLAN_NOT_FOUND);
    }
    
    public static GroupServerException ssafySavingApiError(Throwable cause) {
        return new GroupServerException(GroupServerError.SSAFY_SAVING_API_ERROR, cause);
    }
    
    public static GroupServerException databaseError(Throwable cause) {
        return new GroupServerException(GroupServerError.DATABASE_ERROR, cause);
    }
}
