package com.example.d105.domain.account.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountGroupDisconnectRequest {
    private Long groupId;
    private Long accountId;
    private Long cardId;
}