package com.example.d105.domain.group.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class GroupMemberResponse {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupMemberInfo{
        private Long memberId;
        private Long userId;
        private String username;
        private String displayname;
    }

}
