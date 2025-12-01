package com.example.d105.domain.analysis.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberAnalysisRequest {

    @NotNull(message = "그룹 ID는 필수입니다")
    private Long groupId;

    @NotNull(message = "년월은 필수입니다")
    @Pattern(regexp = "\\d{4}-\\d{2}", message = "년월 형식은 yyyy-MM 이어야 합니다")
    private String yearMonth;
}