package com.example.d105.domain.transaction.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionMonthlyCalendarRequest {
    @NotBlank(message = "년월은 필수입니다")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "년월 형식이 올바르지 않습니다 (YYYY-MM)")
    private String yearMonth; // "2025-09"
}