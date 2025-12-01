package com.example.d105.domain.account.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfo {
    private Long accountId;
    private String accountNo;
    private String bankName;
    private Boolean isConnectedToGroup;
}