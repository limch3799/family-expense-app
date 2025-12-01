package com.example.d105.domain.transaction.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionCategoryChangeRequest {

    @NotNull(message = "거래내역 ID는 필수입니다.")
    private Long transactionId;

    @NotNull(message = "카테고리 ID는 필수입니다.")
    private Short categoryId;
}