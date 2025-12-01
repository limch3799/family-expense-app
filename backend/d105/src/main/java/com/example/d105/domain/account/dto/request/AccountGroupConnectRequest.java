package com.example.d105.domain.account.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AccountGroupConnectRequest {
    private Long groupId;
    private List<Long> accountIds;
    private List<Long> cardIds;
}