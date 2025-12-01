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
public class AccountGroupConnectedListResponse {
    private List<AccountConnectedAccountInfo> myConnectedAccounts;
    private List<AccountConnectedCardInfo> myConnectedCards;
    private String message;
}