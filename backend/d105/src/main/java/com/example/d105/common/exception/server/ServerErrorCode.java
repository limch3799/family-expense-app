package com.example.d105.common.exception.server;

import org.springframework.http.HttpStatus;

/**
 * 모든 서버 오류 코드가 구현해야 하는 기본 인터페이스
 * 각 도메인의 ServerError enum들이 이 인터페이스를 구현함
 */
public interface ServerErrorCode {

    /**
     * 에러 코드 반환 (예: "ACC_S001", "GRP_S002")
     * @return 고유한 에러 코드
     */
    String getCode();

    /**
     * 사용자에게 표시될 에러 메시지 반환
     * @return 에러 메시지
     */
    String getMessage();

    /**
     * HTTP 상태 코드 반환
     * @return HTTP 상태 코드 (500, 502, 503 등)
     */
    HttpStatus getHttpStatus();
}