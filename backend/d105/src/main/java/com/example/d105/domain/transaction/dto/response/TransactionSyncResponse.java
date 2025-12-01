package com.example.d105.domain.transaction.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransactionSyncResponse {
    private String message;
    private Integer newTransactionCount;
}