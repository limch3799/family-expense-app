package com.example.d105.domain.transaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionListRequest {
    @NotBlank(message = "연월을 입력해주세요")
    private String yearMonth; // YYYY-MM 형식

    @Min(value = 0, message = "페이지는 0 이상이어야 합니다")
    private int page = 0;

    @Min(value = 1, message = "크기는 1 이상이어야 합니다")
    private int size = 20;
}