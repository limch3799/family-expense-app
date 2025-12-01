package com.example.d105.domain.transaction.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionExcludeRequest {
    @NotNull(message = "거래내역 ID를 입력해주세요")
    private Long transactionId;
}