package com.example.d105.domain.user.service;

import com.example.d105.domain.user.entity.User;
import com.example.d105.domain.user.exception.UserException;
import com.example.d105.domain.user.repository.UserRepository;
import com.example.d105.common.external.email.EmailSender; // 1. EmailSender 임포트
import com.example.d105.domain.user.dto.request.LoginRequest;
import com.example.d105.domain.user.dto.response.LoginResponse;

import com.example.d105.security.service.CryptoService;
import com.example.d105.security.util.JWTUtil;
import com.example.d105.ssafy.user.service.SsafyUserService;
import com.example.d105.domain.user.dto.request.PasswordResetRequestDto;
import com.example.d105.domain.user.dto.request.PasswordResetConfirmDto; // DTO 임포트
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder; // PasswordEncoder 임포트

import jakarta.mail.MessagingException; // 2. 필요한 클래스 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom; // 2. 필요한 클래스 임포트
import java.util.Map; // 2. 필요한 클래스 임포트
import java.util.concurrent.ConcurrentHashMap; // 2. 필요한 클래스 임포트
import com.example.d105.common.external.sms.SmsSender; // SmsSender 임포트

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService {

//    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;
    private final SsafyUserService ssafyUserService;
    private final EmailSender emailSender; // 3. EmailSender 의존성 주입 추가
    private final SmsSender smsSender; // 1. SmsSender 의존성 주입 추가
    private final CryptoService cryptoService; // CryptoService 의존성 주입 추가
    private final PasswordEncoder passwordEncoder; // PasswordEncoder 의존성 추가

    // =================================================================
    // ▼▼▼▼▼▼▼▼▼▼▼ 이 부분부터 login 메소드 전까지 추가 ▼▼▼▼▼▼▼▼▼▼▼
    // =================================================================

    // 인증 코드를 임시 저장할 저장소 (메모리 기반)
    private final Map<String, String> emailVerificationCodes = new ConcurrentHashMap<>();
    // 2. SMS 인증 코드를 임시 저장할 저장소 추가
    private final Map<String, String> smsVerificationCodes = new ConcurrentHashMap<>();
    /**
     * 이메일 인증 코드 발송 로직
     * @param email 인증 코드를 받을 이메일 주소
     * @throws MessagingException 메일 발송 예외
     */
    public void sendEmailVerification(String email) {
        // 6자리 랜덤 숫자 코드 생성
        String code = String.format("%06d", new SecureRandom().nextInt(1000000));

        // 이메일과 코드를 저장소에 저장
        emailVerificationCodes.put(email, code);

        try {
            // 기존 로직...
            emailSender.sendVerificationEmail(email, code);
        } catch (MessagingException e) {
            log.error("이메일 발송 실패. email: {}", email, e);
            throw new IllegalStateException("이메일 발송 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 이메일 인증 코드 검증 로직
     * @param email 검증할 이메일 주소
     * @param code 사용자가 입력한 인증 코드
     * @return 인증 성공 여부 (true/false)
     */
    public boolean verifyEmailCode(String email, String code) {
        String storedCode = emailVerificationCodes.get(email);

        // 저장된 코드가 있고, 입력된 코드와 일치하는지 확인
        if (storedCode != null && storedCode.equals(code)) {
            emailVerificationCodes.remove(email); // 인증 성공 시, 사용된 코드는 즉시 삭제
            return true;
        }
        return false;
    }
    /**
     * SMS 인증 코드 발송
     * @param phoneNumber 인증 코드를 받을 전화번호
     */
    public void sendSmsVerification(String phoneNumber) {
        // 6자리 랜덤 숫자 코드 생성
        String code = String.format("%06d", new SecureRandom().nextInt(1000000));

        // 전화번호와 코드를 저장소에 저장
        smsVerificationCodes.put(phoneNumber, code);

        // SMS 발송
        smsSender.sendSms(phoneNumber, code);
    }

    /**
     * SMS 인증 코드 검증
     * @param phoneNumber 검증할 전화번호
     * @param code 사용자가 입력한 인증 코드
     * @return 인증 성공 여부
     */
    public boolean verifySmsCode(String phoneNumber, String code) {
        String storedCode = smsVerificationCodes.get(phoneNumber);

        if (storedCode != null && storedCode.equals(code)) {
            smsVerificationCodes.remove(phoneNumber); // 인증 성공 시 코드 삭제
            return true;
        }
        return false;
    }
    public void requestPasswordReset(PasswordResetRequestDto requestDto) {
        // 2. 이메일로 사용자를 먼저 찾습니다.
        User user = userRepository.findByEmailAndDeletedAtIsNull(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("해당하는 사용자 정보가 없습니다."));

        // 3. DB에 저장된 암호화된 이름과 전화번호를 복호화합니다.
        String decryptedUsername = cryptoService.decryptAES(user.getUsername());
        String decryptedPhoneNumber = cryptoService.decryptAES(user.getPhoneNumber());

        // 4. 입력된 정보와 복호화된 정보가 일치하는지 확인합니다.
        if (!decryptedUsername.equals(requestDto.getUsername()) ||
                !decryptedPhoneNumber.equals(requestDto.getPhoneNumber())) {
            throw new IllegalArgumentException("사용자 정보가 일치하지 않습니다.");
        }

        // 5. 모든 정보가 일치하면, 기존에 만들어둔 이메일 발송 기능을 재사용합니다.
        sendEmailVerification(requestDto.getEmail());
    }

    @Transactional // 데이터를 변경하므로 readOnly = false (기본값)
    public void confirmPasswordReset(PasswordResetConfirmDto requestDto) {
        // 1. 이메일과 인증 코드가 유효한지 먼저 확인합니다.
        boolean isVerified = verifyEmailCode(requestDto.getEmail(), requestDto.getVerificationCode());
        if (!isVerified) {
            throw new IllegalArgumentException("인증 코드가 올바르지 않거나 만료되었습니다.");
        }

        // 2. 인증이 성공하면, 해당 이메일의 사용자를 다시 찾습니다.
        User user = userRepository.findByEmailAndDeletedAtIsNull(requestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("해당하는 사용자 정보가 없습니다."));

        // 3. 새 비밀번호를 암호화하여 사용자 정보에 업데이트(덮어쓰기)합니다.
        user.setPassword(passwordEncoder.encode(requestDto.getNewPassword()));

        // 4. userRepository.save()를 호출할 필요가 없습니다.
        //    @Transactional 어노테이션 덕분에, 이 메소드가 끝나면
        //    Spring이 변경된 user 객체를 감지하여 자동으로 DB에 UPDATE 쿼리를 실행해줍니다.
    }



    public LoginResponse.LoginResponseDTO login(LoginRequest.LoginRequestDTO loginRequest){
        try{
            // 1. 사용자 존재 여부 확인
            User user = userRepository.findByEmailAndDeletedAtIsNull(loginRequest.getEmail())
                    .orElseThrow(() -> new UserException("USER_NOT_FOUNT", "아이디 또는 비밀번호가 일치하지 않음"));

            // 2. 비밀번호 검증 (올바른 순서: 평문, 암호화된 비밀번호)
            if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new UserException("USER_NOT_FOUNT", "아이디 또는 비밀번호가 일치하지 않음");
            }


            // DB에서 가져온 암호화된 userKey를 복호화
            String decryptedUserKey = cryptoService.decryptAES(user.getUserKey());
            System.out.println("DB에서 복호화된 userKey: " + decryptedUserKey);

            // SSAFY API에서 받은 userKey
            String apiUserKey = ssafyUserService.getSearchWithCode(user.getEmail()).getUserKey();
            System.out.println("API에서 받은 userKey: " + apiUserKey);

            // 복호화된 값과 API 응답 값을 비교
            if(!decryptedUserKey.equals(apiUserKey)){
                throw new IllegalArgumentException("userKey가 일치하지 않습니다.");
            }

            //JWT 토큰 생성
            String accessToken = jwtUtil.createToken(user.getEmail());

            return LoginResponse.LoginResponseDTO.builder()
                    .accessToken(accessToken)
                    .userId(user.getUserId())
                    .build();

        }catch (AuthenticationException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다.");
        }
    }

    //간편 비밀번호 로그인
    public LoginResponse.LoginResponseDTO simpleLogin(LoginRequest.SimpleLoginRequestDTO request){
        try{
            // 1. 사용자 존재 여부 확인
            User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

            if(!cryptoService.verifySimplePassword(request.getSimplePassword() ,user.getSimplePassword()))
                throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");

            String decryptedUserKey = cryptoService.decryptAES(user.getUserKey());
            System.out.println("DB에서 복호화된 userKey: " + decryptedUserKey);

            // SSAFY API에서 받은 userKey
            String apiUserKey = ssafyUserService.getSearchWithCode(user.getEmail()).getUserKey();
            System.out.println("API에서 받은 userKey: " + apiUserKey);

            // 복호화된 값과 API 응답 값을 비교
            if(!decryptedUserKey.equals(apiUserKey)){
                throw new IllegalArgumentException("userKey가 일치하지 않습니다.");
            }

            //JWT 토큰 생성
            String accessToken = jwtUtil.createToken(user.getEmail());

            return LoginResponse.LoginResponseDTO.builder()
                    .accessToken(accessToken)
                    .userId(user.getUserId())
                    .build();

        }catch (AuthenticationException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("이메일 또는 비밀번호가 잘못되었습니다.");
        }
    }
}