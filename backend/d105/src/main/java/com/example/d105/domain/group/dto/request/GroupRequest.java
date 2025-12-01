package com.example.d105.domain.group.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class GroupRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class createGroupDTO {
       private String name;
       private String description;
       private Integer imgId;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupInfoByCode{
        private String invitationCode;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupId{
        private Long groupId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModifyGroupInfo{
        private Long groupId;
        private Integer imgId;
        private String name;
        private String description;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateDeposit{
        private Long groupId;
        private Long balance;
    }


}
