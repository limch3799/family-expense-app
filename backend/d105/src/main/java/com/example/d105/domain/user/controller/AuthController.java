package com.example.d105.domain.user.controller;

import com.example.d105.domain.user.repository.UserRepository;
import com.example.d105.domain.user.service.UserService;
import com.example.d105.domain.user.dto.request.LoginRequest;

import com.example.d105.domain.user.dto.response.LoginResponse;

import com.example.d105.domain.user.dto.response.EmailConfirmDto;
import com.example.d105.domain.user.dto.request.EmailRequestDto;
import com.example.d105.domain.user.dto.response.SmsConfirmDto;
import com.example.d105.domain.user.dto.request.SmsRequestDto;
import com.example.d105.domain.user.dto.request.PasswordResetRequestDto;
import com.example.d105.domain.user.dto.request.PasswordResetConfirmDto;
import com.example.d105.domain.user.service.AuthService;
import com.example.d105.security.service.CryptoService;
import com.example.d105.ssafy.user.service.SsafyUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "1. 인증", description = "Authentication")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final SsafyUserService ssafyUserService;
    private final CryptoService cryptoService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, String> redisTemplate;
    // ====================== 로그인 / 로그아웃 ======================

    @Operation(
            summary = "사용자 로그인",
            description = "사용자의 이메일과 비밀번호를 받아 인증 후 JWT access token을 발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = LoginRequest.LoginRequestDTO.class))),
                    @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 잘못됨")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<LoginResponse.LoginResponseDTO> login(@Valid @RequestBody LoginRequest.LoginRequestDTO loginRequest) {
        LoginResponse.LoginResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "간편 로그인",
            description = "사용자의 이메일과 간편비밀번호를 받아 인증 후 JWT access token을 발급합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "로그인 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = LoginRequest.SimpleLoginRequestDTO.class))),
                    @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 잘못됨")
            }
    )
    @PostMapping("/simple-login")
    public ResponseEntity<LoginResponse.LoginResponseDTO> simpleLogin(@Valid @RequestBody LoginRequest.SimpleLoginRequestDTO loginRequest) {
        LoginResponse.LoginResponseDTO response = authService.simpleLogin(loginRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "사용자 로그아웃",
            description = "로그아웃을 진행합니다. (백엔드에서 별도 처리 없음, 프론트에서 토큰 삭제)"
    )
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("로그아웃 되었습니다");
    }

    // ====================== 회원가입 ======================

//    @Operation(
//            summary = "임시 회원가입",
//            description = "임시 회원가입 API"
//    )
//    @PostMapping("/signup")
//    public void signup(@RequestBody UserRequest.UserSignupRequest req) {
//        String userKey = ssafyApiService.getMemberWithCode(req.getEmail()).getUserKey();
//        System.out.println("userKey : " + userKey);
//
//        User user = new User();
//        user.setEmail(req.getEmail());
//
//        if (req.getSimplePassword() != null) {
//            user.setSimplePassword(cryptoService.encryptSimplePassword(req.getSimplePassword()));
//        }
//        if (req.getPhoneNumber() != null) {
//            user.setPhoneNumber(cryptoService.encryptAES(req.getPhoneNumber()));
//        }
//
//        user.setUserKey(cryptoService.encryptAES(userKey));
//        user.setPassword(passwordEncoder.encode(req.getPassword()));
//        user.setUsername(cryptoService.encryptAES(req.getUsername()));
//        user.setBirthDate(cryptoService.encryptAES(req.getBirthDate()));
//        user.setGender(cryptoService.encryptAES(req.getGender()));
//        user.setCreatedAt(String.valueOf(System.currentTimeMillis()));
//        user.setUpdatedAt(String.valueOf(System.currentTimeMillis()));
//
//        userRepository.save(user);
//    }

    // ====================== 이메일 인증 ======================

    @Operation(
            summary = "회원가입 이메일 인증 코드 발송",
            description = "지정된 이메일로 6자리 인증 코드를 발송합니다."
    )
    @PostMapping("/verify/email/send")
    public ResponseEntity<Map<String, Object>> sendVerificationEmail(@RequestBody EmailRequestDto requestDto) {
        Map<String, Object> response = new HashMap<>();

        String email = requestDto.getEmail();
        authService.sendEmailVerification(email);
        response.put("success", true);
        response.put("message", "인증 코드가 " + email + "로 발송되었습니다.");
        return ResponseEntity.ok(response);

    }
    @Operation(
            summary = "비밀번호 재설정 요청 (본인 확인)",
            description = "이름, 전화번호, 이메일로 본인 확인 후 인증 메일을 발송합니다."
    )
    @PostMapping("/password/reset/request")
    public ResponseEntity<Map<String, Object>> requestPasswordReset(@RequestBody PasswordResetRequestDto requestDto) {
        Map<String, Object> response = new HashMap<>();

        authService.requestPasswordReset(requestDto);
        response.put("success", true);
        response.put("message", "인증 코드가 이메일로 발송되었습니다.");
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "비밀번호 재설정 확인 및 변경",
            description = "이메일로 받은 인증 코드와 새 비밀번호를 받아 비밀번호를 최종적으로 변경합니다."
    )
    @PostMapping("/password/reset/confirm")
    public ResponseEntity<Map<String, Object>> confirmPasswordReset(@RequestBody PasswordResetConfirmDto requestDto) {
        Map<String, Object> response = new HashMap<>();

        authService.confirmPasswordReset(requestDto);
        response.put("success", true);
        response.put("message", "비밀번호가 성공적으로 재설정되었습니다.");
        return ResponseEntity.ok(response);

    }


    @Operation(
            summary = "회원가입 이메일 인증 코드 확인",
            description = "이메일로 발송된 6자리 인증 코드를 확인합니다."
    )
    @PostMapping("/verify/email/confirm")
    public ResponseEntity<Map<String, Object>> confirmVerificationEmail(@RequestBody EmailConfirmDto requestDto) {
        Map<String, Object> response = new HashMap<>();
        boolean isVerified = authService.verifyEmailCode(requestDto.getEmail(), requestDto.getCode());

        if (isVerified) {
            response.put("success", true);
            response.put("message", "이메일 인증이 완료되었습니다.");
            //레디스에 해당 회원이 이메일인증했음 여부를 저장
            String redisKey = "email_verified: "+ requestDto.getEmail();
            redisTemplate.opsForValue().set(redisKey, "true", Duration.ofMinutes(5));


            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "인증 코드가 올바르지 않거나 만료되었습니다.");

            //레디스에 해당 회원이 이메일인증했음 여부를 저장
            String redisKey = "email_verified: "+ requestDto.getEmail();
            redisTemplate.opsForValue().set(redisKey, "true", Duration.ofMinutes(5));

            return ResponseEntity.badRequest().body(response);
        }
    }

    // ====================== SMS 인증 ======================

    @Operation(
            summary = "회원가입,회원탈퇴 등 SMS 인증 코드 발송",
            description = "지정된 전화번호로 6자리 인증 코드를 발송합니다."
    )
    @PostMapping("/verify/sms/send")
    public ResponseEntity<Map<String, Object>> sendSmsVerification(@RequestBody SmsRequestDto requestDto) {
        Map<String, Object> response = new HashMap<>();

        String phoneNumber = requestDto.getPhoneNumber();
        authService.sendSmsVerification(phoneNumber);
        response.put("success", true);
        response.put("message", "인증 코드가 " + phoneNumber + "로 발송되었습니다.");
        return ResponseEntity.ok(response);

    }

    @Operation(
            summary = "회원가입 SMS 인증 코드 확인",
            description = "전화번호로 발송된 6자리 인증 코드를 확인합니다."
    )
    @PostMapping("/verify/sms/confirm")
    public ResponseEntity<Map<String, Object>> confirmSmsVerification(@RequestBody SmsConfirmDto requestDto) {
        Map<String, Object> response = new HashMap<>();
        boolean isVerified = authService.verifySmsCode(requestDto.getPhoneNumber(), requestDto.getCode());

        if (isVerified) {
            response.put("success", true);
            response.put("message", "SMS 인증이 완료되었습니다.");

            String redisKey = "phone_verified: "+ requestDto.getPhoneNumber();
            redisTemplate.opsForValue().set(redisKey, "true", Duration.ofMinutes(5));


            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "인증 코드가 올바르지 않거나 만료되었습니다.");

            //추후 삭제할거임 ----------------------
            String redisKey = "phone_verified: "+ requestDto.getPhoneNumber();
            redisTemplate.opsForValue().set(redisKey, "true", Duration.ofMinutes(5));
            //추후 삭제할거임 ----------------------
            return ResponseEntity.badRequest().body(response);
        }
    }
}
