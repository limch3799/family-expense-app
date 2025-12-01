package com.example.d105.domain.analysis.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Builder
public class GroupLastUpdatedResponse {
    private Long groupId;
    private String groupName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private ZonedDateTime lastUpdated;  // 그룹 마지막 업데이트 시각

    public static GroupLastUpdatedResponse of(Long groupId, String groupName,
                                              ZonedDateTime lastUpdated) {
        return GroupLastUpdatedResponse.builder()
                .groupId(groupId)
                .groupName(groupName)
                .lastUpdated(lastUpdated)
                .build();
    }
}
