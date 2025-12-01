package com.example.d105.domain.group.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

public class GroupResponse {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupInfoByCode{
        private String savingsAccountNo;
        private String name;
        private String description;
        private String ownerName;
        private Long memberCount;

    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupInfo{
        private String groupName;
        private String groupDescription;
        private String savingAccountNo;
        private String inviteCode;
        private int amount;
        private Integer imgId;
        private Long planId;
        private List<GroupMemberResponse.GroupMemberInfo> joinedMembers;
        private List<GroupMemberResponse.GroupMemberInfo> waitingMember;
        private String createdAt;
    }
}
