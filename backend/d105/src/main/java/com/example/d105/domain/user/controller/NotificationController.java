package com.example.d105.domain.user.controller;

import com.example.d105.domain.user.dto.request.NotificationRequest;
import com.example.d105.domain.user.dto.request.TokenRequest;
import com.example.d105.domain.user.dto.response.NotificationResponse;
import com.example.d105.domain.user.service.NotificationService;
import com.example.d105.security.dto.CustomUserDetails;
import com.google.firebase.messaging.FirebaseMessagingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notification")
@Tag(name = "8. 알림설정관리" ,  description = "Notification Management")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "알림 설정 변경",
            description = "알림 설정을 변경합니다."
    )
    @PostMapping("/setting/update")
    public ResponseEntity<Void> updateSetting(@AuthenticationPrincipal CustomUserDetails user,  @RequestBody NotificationRequest.SettingNotificationRequest request) {
        notificationService.notificationSetting(user.getUser().getUserId(), request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "알림 설정 조회",
            description = "알림 설정을 조회합니다."
    )
    @PostMapping("/setting/info")
    public ResponseEntity<NotificationResponse.NotificationSettingInfo> settingInfo(@AuthenticationPrincipal CustomUserDetails user) {

        return ResponseEntity.ok(notificationService.getInfo(user.getUser().getUserId()));

    }
}
