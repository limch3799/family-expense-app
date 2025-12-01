package com.example.d105.common.exception.server;

import org.springframework.http.HttpStatus;

/**
 * 모든 서버 오류 예외의 기본 클래스
 * 각 도메인의 ServerException들이 이 클래스를 상속함
 */
public abstract class ServerException extends RuntimeException {

    private final ServerErrorCode errorCode;
    private final Object[] args;  // 메시지 파라미터 (선택적)

    /**
     * 기본 생성자
     * @param errorCode 에러 코드 enum
     */
    protected ServerException(ServerErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = new Object[0];
    }

    /**
     * 원인 예외와 함께 생성
     * @param errorCode 에러 코드 enum
     * @param cause 원인 예외
     */
    protected ServerException(ServerErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.args = new Object[0];
    }

    /**
     * 메시지 파라미터와 함께 생성
     * @param errorCode 에러 코드 enum
     * @param args 메시지 파라미터들
     */
    protected ServerException(ServerErrorCode errorCode, Object... args) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.args = args;
    }

    /**
     * 원인 예외와 메시지 파라미터 모두 포함
     * @param errorCode 에러 코드 enum
     * @param cause 원인 예외
     * @param args 메시지 파라미터들
     */
    protected ServerException(ServerErrorCode errorCode, Throwable cause, Object... args) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.args = args;
    }

    /**
     * 에러 코드 반환
     * @return ServerErrorCode 구현체
     */
    public ServerErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * HTTP 상태 코드 반환
     * @return HTTP 상태 코드
     */
    public HttpStatus getHttpStatus() {
        return errorCode.getHttpStatus();
    }

    /**
     * 메시지 파라미터들 반환
     * @return 파라미터 배열
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * 에러 코드 문자열 반환 (편의 메소드)
     * @return 에러 코드 문자열
     */
    public String getCode() {
        return errorCode.getCode();
    }
}