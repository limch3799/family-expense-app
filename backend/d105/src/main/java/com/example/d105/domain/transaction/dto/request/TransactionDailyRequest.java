package com.example.d105.domain.transaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionDailyRequest {
    @NotBlank(message = "날짜를 입력해주세요")
    private String date; // YYYY-MM-DD 형식
}