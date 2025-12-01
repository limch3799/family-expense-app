package com.example.d105.domain.user.controller;

import com.example.d105.domain.user.dto.request.UserRequest;
import com.example.d105.domain.user.dto.request.UserWithdrawalRequestDto;
import com.example.d105.domain.user.dto.response.UserInfoResponseDto;
import com.example.d105.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.Map; // Map 임포트
import java.util.HashMap; // HashMap 임포트

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "1. 인증", description = "Authentication")
public class UserController {

    // 1. 이제 UserService 하나만 있으면 됩니다.
    private final UserService userService;

    @Operation(
            summary = "회원가입",
            description = "사용자 정보를 받아 회원가입을 처리합니다."
    )
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserRequest.UserSignupRequest request) {
        // 2. 모든 로직을 UserService에 위임합니다.
        userService.signUp(request);
        return ResponseEntity.ok("회원가입 성공");
    }



    @Operation(summary = "내 정보 조회", description = "로그인된 사용자의 정보를 조회합니다.")
    @PostMapping("/me/info")
    public ResponseEntity<UserInfoResponseDto> getMyInfo() {
        UserInfoResponseDto userInfo = userService.getMyInfo();
        return ResponseEntity.ok(userInfo);
    }

    @Operation(summary = "회원 탈퇴", description = "SMS 인증 후 회원 탈퇴를 처리합니다.")
    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(@RequestBody UserWithdrawalRequestDto requestDto) {
        Map<String, Object> response = new HashMap<>();

        userService.withdraw(requestDto);
        response.put("success", true);
        response.put("message", "회원 탈퇴가 완료되었습니다.");
        return ResponseEntity.ok(response);

    }

    @PostMapping("/validate/email")
    public ResponseEntity<Map<String, Object>> validateEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        boolean exists = userService.isEmailExists(email);

        Map<String, Object> response = new HashMap<>();
        response.put("available", !exists);
        response.put("message", exists ? "이미 사용중인 이메일입니다" : "사용 가능한 이메일입니다");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/validate/phone")
    public ResponseEntity<Map<String, Object>> validatePhoneNumber(@RequestBody Map<String, String> request) {
        String phoneNumber = request.get("phoneNumber");
        boolean exists = userService.isPhoneNumberExists(phoneNumber);

        Map<String, Object> response = new HashMap<>();
        response.put("available", !exists);
        response.put("message", exists ? "이미 사용중인 전화번호입니다" : "사용 가능한 전화번호입니다");

        return ResponseEntity.ok(response);
    }


}