package com.example.d105.domain.account.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AccountConnectionResult {
    private Long accountId;
    private Long cardId;
    private String status;
    private String message;

    public static AccountConnectionResult accountSuccess(Long accountId) {
        return AccountConnectionResult.builder()
                .accountId(accountId)
                .status("success")
                .message("계좌 연결 완료")
                .build();
    }

    public static AccountConnectionResult accountFailure(Long accountId, String errorMessage) {
        return AccountConnectionResult.builder()
                .accountId(accountId)
                .status("failure")
                .message(errorMessage)
                .build();
    }

    public static AccountConnectionResult cardSuccess(Long cardId) {
        return AccountConnectionResult.builder()
                .cardId(cardId)
                .status("success")
                .message("카드 연결 완료")
                .build();
    }

    public static AccountConnectionResult cardFailure(Long cardId, String errorMessage) {
        return AccountConnectionResult.builder()
                .cardId(cardId)
                .status("failure")
                .message(errorMessage)
                .build();
    }
}