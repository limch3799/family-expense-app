package com.example.d105.domain.account.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountListResponse {
    private List<AccountInfo> accounts;
    private List<CardInfo> cards;
    private String message;
}