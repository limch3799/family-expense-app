package com.example.d105.domain.analysis.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GroupTotalExpenseRequest {
    @NotNull(message = "그룹 ID는 필수입니다")
    @Positive(message = "그룹 ID는 양수여야 합니다")
    private Long groupId;

    @NotBlank(message = "년월은 필수입니다")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "년월 형식이 올바르지 않습니다 (YYYY-MM)")
    private String yearMonth;
}