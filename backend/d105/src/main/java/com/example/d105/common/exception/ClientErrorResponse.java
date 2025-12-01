package com.example.d105.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 4xx 클라이언트 오류 전용 응답 클래스
 * 리다이렉트 URL 등 클라이언트 오류 전용 정보 포함
 */
@Getter
@Builder
@AllArgsConstructor
public class ClientErrorResponse {
    private int status;           // 401, 403, 404 등
    private String message;       // 에러 메시지
    private String redirectUrl;   // 리다이렉트 URL (선택적)
    private long timestamp;       // 에러 발생 시각

    /**
     * 기본 클라이언트 에러 응답 생성
     */
    public static ClientErrorResponse of(HttpStatus status, String message) {
        return ClientErrorResponse.builder()
                .status(status.value())
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 리다이렉트 URL을 포함한 클라이언트 에러 응답 생성
     */
    public static ClientErrorResponse withRedirect(HttpStatus status, String message, String redirectUrl) {
        return ClientErrorResponse.builder()
                .status(status.value())
                .message(message)
                .redirectUrl(redirectUrl)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}