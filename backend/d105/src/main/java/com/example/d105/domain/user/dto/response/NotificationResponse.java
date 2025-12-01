package com.example.d105.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class NotificationResponse {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationSettingInfo{
        private Boolean planerPushEnabled;
        private Boolean reporterPushEnabled;
        private Boolean transactionPushEnabled;
    }
}
