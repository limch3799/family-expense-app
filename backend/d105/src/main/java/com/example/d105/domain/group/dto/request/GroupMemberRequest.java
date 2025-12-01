package com.example.d105.domain.group.dto.request;

import com.example.d105.domain.group.entity.Group;
import com.example.d105.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class GroupMemberRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class createGroupMemberDTO {
        private  String inviteCode;
        private String displayName;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllowGroupMemberDTO{
        private Long groupId;
        private Long memberId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateMemberDisplayRequest{
       private Long memberId;
       private  String displayName;
    }
}
