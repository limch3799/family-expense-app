package com.example.d105.domain.analysis.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GroupRecentTrendRequest {
    @NotNull(message = "그룹 ID는 필수입니다")
    @Positive(message = "그룹 ID는 양수여야 합니다")
    private Long groupId;
}