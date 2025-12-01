package com.example.d105.common.exception;

import com.example.d105.domain.analysis.exception.AnalysisServerException;
import com.example.d105.domain.group.exception.GroupException;
import com.example.d105.domain.group.exception.GroupServerException;
import com.example.d105.domain.report.exception.ReportException;
import com.example.d105.domain.transaction.exception.TransactionServerException;
import com.example.d105.domain.user.exception.UserException;
import com.example.d105.domain.user.exception.UserServerException;
import com.example.d105.ssafy.exception.SsafyApiException;
import com.example.d105.ssafy.exception.SsafyServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.d105.domain.account.exception.AccountServerException;
import com.example.d105.common.exception.server.ServerException;
import com.example.d105.common.exception.ResourceNotFoundException;
import com.example.d105.common.exception.ClientErrorResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.format.DateTimeParseException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ==================== 4xx 클라이언트 오류 ====================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ClientErrorResponse> handleResourceNotFound(ResourceNotFoundException e) {
        log.warn("Resource not found: {}", e.getMessage());

        ClientErrorResponse response = ClientErrorResponse.of(
                HttpStatus.NOT_FOUND,
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ClientErrorResponse> handleAccessDenied(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());
        log.warn("AccessDeniedException handler called: {}", e.getMessage()); // 추가
        log.warn("Exception class: {}", e.getClass().getName()); // 추가

        ClientErrorResponse response = ClientErrorResponse.of(
                HttpStatus.FORBIDDEN,
                "접근 권한이 없습니다."
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ClientErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Illegal argument: {}", e.getMessage());

        ClientErrorResponse response = ClientErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ClientErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getMessage());

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + ", " + msg2)
                .orElse("입력값이 올바르지 않습니다.");

        ClientErrorResponse response = ClientErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                errorMessage
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

// ==================== 5xx 서버 오류 ====================

    @ExceptionHandler(ServerException.class)
    public ResponseEntity<ErrorResponse> handleServerException(ServerException e) {
        log.error("Server exception occurred: {} - {}", e.getCode(), e.getMessage(), e);

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(e.getCode())
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }



    @ExceptionHandler(SsafyApiException.class)
    public ResponseEntity<ErrorResponse> handleSsafyApiException(SsafyApiException e) {
        log.error("Ssafy API Exception: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("SSAFY_API_ERROR")
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(GroupException.class)
    public ResponseEntity<ErrorResponse> handleGroupException(GroupException e) {
        log.error("Group Exception: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(e.getErrorCode())
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ReportException.class)
    public ResponseEntity<ErrorResponse> handleReportException(ReportException e) {
        log.error("Group Exception: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(e.getErrorCode())
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


    @ExceptionHandler(AccountServerException.class)
    public ResponseEntity<ErrorResponse> handleAccountServerException(AccountServerException e) {
        log.error("Account Server Exception: {} - {}", e.getCode(), e.getMessage(), e);

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(e.getCode())
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    @ExceptionHandler(AnalysisServerException.class)
    public ResponseEntity<ErrorResponse> handleAnalysisServerException(AnalysisServerException e) {
        log.error("Analysis Server Exception: {} - {}", e.getCode(), e.getMessage(), e);

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(e.getCode())
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    @ExceptionHandler(TransactionServerException.class)
    public ResponseEntity<ErrorResponse> handleTransactionServerException(TransactionServerException e) {
        log.error("Transaction Server Exception: {} - {}", e.getCode(), e.getMessage(), e);

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(e.getCode())
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    @ExceptionHandler(UserServerException.class)
    public ResponseEntity<ErrorResponse> handleUserServerException(UserServerException e) {
        log.error("User Server Exception: {} - {}", e.getCode(), e.getMessage(), e);

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(e.getCode())
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    @ExceptionHandler(SsafyServerException.class)
    public ResponseEntity<ErrorResponse> handleSsafyServerException(SsafyServerException e) {
        log.error("Ssafy Server Exception: {} - {}", e.getCode(), e.getMessage(), e);

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(e.getCode())
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    @ExceptionHandler(GroupServerException.class)
    public ResponseEntity<ErrorResponse> handleGroupServerException(GroupServerException e) {
        log.error("Group Server Exception: {} - {}", e.getCode(), e.getMessage(), e);

        ErrorResponse response = ErrorResponse.builder()
                .errorCode(e.getCode())
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    // GlobalExceptionHandler에만 추가 (한 번만 작성)
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ClientErrorResponse> handleDateTimeParseException(DateTimeParseException e) {
        log.warn("Invalid date format: {}", e.getMessage());

        ClientErrorResponse response = ClientErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                "올바르지 않은 날짜 형식입니다"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleReportException(UserException e) {
        log.error("Group Exception: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(e.getErrorCode())
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException e) {
        log.error("System state error: {}", e.getMessage(), e);

        ErrorResponse response = ErrorResponse.of("SYSTEM_ERROR", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("Unexpected Exception: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode("INTERNAL_SERVER_ERROR")
                .message("서버 내부 오류가 발생했습니다.")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}