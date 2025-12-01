package com.example.d105.domain.user.controller;

import com.example.d105.domain.user.dto.request.TokenRequest;
import com.example.d105.domain.user.dto.request.UserRequest;
import com.example.d105.domain.user.service.FcmService;
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
@RequestMapping("/api/v1/fcm")
@Tag(name = "7. FCM 토큰 관리" ,  description = "Fcm Token Management")
public class FcmController {

    private final FcmService fcmService;


    @Operation(
            summary = "fcm토큰 등록",
            description = "fcm토큰을 등록합니다"
    )
    @PostMapping("/register")
    public ResponseEntity<Void> registerToken(@RequestBody TokenRequest.CreateTokenRequest request) {
    fcmService.createFCMToken(71L, request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "토큰으로 알림보내기 ( 임시 )",
            description = "토큰으로 알림을 보냅니다."
    )
    @PostMapping("/send")
    public ResponseEntity<Void> sendMessage(@RequestBody TokenRequest.SendMessageRequest request) throws FirebaseMessagingException {
        fcmService.sendMessage(request);
        return ResponseEntity.ok().build();
    }


    @Operation(
            summary = "토큰으로 플랜생성시 알림보내기 ( 임시 )",
            description = "토큰으로 알림을 보냅니다."
    )
    @PostMapping("/send/plan/create")
    public ResponseEntity<Void> sendPlanCreateMessage(@RequestBody TokenRequest.SendMessageByGroupRequest request) throws FirebaseMessagingException {
        fcmService.createPlannerSendMessage(request);
        return ResponseEntity.ok().build();
    }


}
