package com.example.d105.domain.user.dto.request;

import lombok.*;

public class NotificationRequest {

    @Data
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SettingNotificationRequest{
        private Boolean planerPushEnabled;
        private Boolean reporterPushEnabled;
        private Boolean transactionPushEnabled;
    }
}
