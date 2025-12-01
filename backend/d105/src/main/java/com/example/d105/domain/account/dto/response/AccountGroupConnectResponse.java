package com.example.d105.domain.account.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class AccountGroupConnectResponse {
    private List<AccountConnectionResult> connectedAccounts;
    private List<AccountConnectionResult> connectedCards;
    private String message;
}