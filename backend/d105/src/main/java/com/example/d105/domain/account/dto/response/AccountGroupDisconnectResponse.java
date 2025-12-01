package com.example.d105.domain.account.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AccountGroupDisconnectResponse {
    private String status;
    private String message;
    private String disconnectedType;
    private Long disconnectedId;

    public static AccountGroupDisconnectResponse accountSuccess(Long accountId) {
        return AccountGroupDisconnectResponse.builder()
                .status("success")
                .message("연결 해제가 완료되었습니다.")
                .disconnectedType("account")
                .disconnectedId(accountId)
                .build();
    }

    public static AccountGroupDisconnectResponse cardSuccess(Long cardId) {
        return AccountGroupDisconnectResponse.builder()
                .status("success")
                .message("연결 해제가 완료되었습니다.")
                .disconnectedType("card")
                .disconnectedId(cardId)
                .build();
    }
}