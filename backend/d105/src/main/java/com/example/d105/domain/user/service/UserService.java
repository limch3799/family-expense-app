package com.example.d105.domain.user.service;

import com.example.d105.domain.group.repository.GroupMemberRepository;
import com.example.d105.domain.group.entity.GroupMember;
import com.example.d105.domain.user.dto.request.UserRequest;
import com.example.d105.domain.user.dto.response.UserInfoResponseDto;
import com.example.d105.domain.user.entity.User;
import com.example.d105.domain.user.exception.UserException;
import com.example.d105.domain.user.exception.UserServerException;
import com.example.d105.domain.user.repository.UserRepository;
import com.example.d105.security.service.CryptoService;
import com.example.d105.ssafy.user.dto.SsafyUserRequest;
import com.example.d105.ssafy.user.service.SsafyUserService;
import com.example.d105.domain.user.dto.request.UserWithdrawalRequestDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CryptoService cryptoService;
    private final PasswordEncoder passwordEncoder;
    private final SsafyUserService ssafyUserService;
    private final AuthService authService;

    private final RedisTemplate<String, String> redisTemplate;

    public void signUp(UserRequest.UserSignupRequest request) {

        if(isEmailVerified(request.getEmail()) && isPhoneVerified(request.getPhoneNumber())){
            // 1. UserSignupRequest DTO를 SsafyUserRequest.RegistUser DTO로 변환
            SsafyUserRequest.RegistUser ssafyRequest = SsafyUserRequest.RegistUser.from(request);

            // 2. SsafyApiService를 호출하여 SSAFY 서버에 멤버를 생성하고 userKey를 받아옴
            String userKey = ssafyUserService.registUser(ssafyRequest);

            // 3. 받아온 userKey로 우리 DB에 사용자를 저장
            User user = new User();
            user.setEmail(request.getEmail());
            if (request.getSimplePassword() != null) {
                user.setSimplePassword(cryptoService.encryptSimplePassword(request.getSimplePassword()));
            }
            if (request.getPhoneNumber() != null) {
                user.setPhoneNumber(cryptoService.encryptAES(request.getPhoneNumber()));
            }
            user.setUserKey(cryptoService.encryptAES(userKey));
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setUsername(cryptoService.encryptAES(request.getUsername()));
            user.setBirthDate(cryptoService.encryptAES(request.getBirthDate()));
            user.setGender(cryptoService.encryptAES(request.getGender()));
            user.setCreatedAt(String.valueOf(System.currentTimeMillis()));
            user.setUpdatedAt(String.valueOf(System.currentTimeMillis()));

            userRepository.save(user);

        }else{
            throw new UserException("INVALID_VERIFICATION_CODE", "인증 코드가 올바르지 않거나 만료되었습니다.");
        }





    }

    private boolean isEmailVerified(String email){
        String redisKey = "email_verified: " + email;
        String verified = redisTemplate.opsForValue().get(redisKey);
        System.out.println("email_verified " + verified);
        return "true".equals(verified);
    }

    private boolean isPhoneVerified(String phone){
        String redisKey = "phone_verified: "+ phone;
        String verified = redisTemplate.opsForValue().get(redisKey);
        System.out.println("phone_verified" + verified);
        return "true".equals(verified);
    }



    @Transactional(readOnly = true)
    public String getUserKey(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다"));

        return cryptoService.decryptAES(user.getUserKey());
    }

    @Transactional(readOnly = true)
    public UserInfoResponseDto getMyInfo() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmailAndDeletedAtIsNull(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("로그인 유저 정보가 없습니다."));

        // 사용자의 활성 그룹 조회
        Long groupId = null;
        try {

            List<GroupMember> groupMembers = groupMemberRepository.findByUser(user);

            for(GroupMember member : groupMembers){
                if(member.getAllowedAt() != null && member.getExitedAt() == null)
                {
                    groupId = member.getGroup().getGroupId();
                    break;
                }

            }
        } catch (Exception e) {
            // 그룹이 없는 경우는 정상적인 상황이므로 null로 처리
            groupId = null;
        }

        return UserInfoResponseDto.from(user, groupId, cryptoService);
    }

    @Transactional(readOnly = true)
    public String getUserName(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다"));

        return cryptoService.decryptAES(user.getUsername());
    }

    public void withdraw(UserWithdrawalRequestDto requestDto) {
        // 현재 로그인한 사용자의 정보를 가져옵니다.
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmailAndDeletedAtIsNull(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("로그인 유저 정보가 없습니다."));

        // 사용자의 전화번호를 복호화합니다.
        String phoneNumber = cryptoService.decryptAES(user.getPhoneNumber());

        // AuthService의 SMS 인증 코드 검증 기능을 재사용합니다.
        boolean isVerified = authService.verifySmsCode(phoneNumber, requestDto.getVerificationCode());
        if (!isVerified) {
            throw new IllegalArgumentException("SMS 인증 코드가 올바르지 않습니다.");
        }

        // 논리적 삭제: deletedAt 필드에 현재 시간을 기록합니다.
        user.setDeletedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // @Transactional에 의해 메소드가 끝나면 자동으로 UPDATE 쿼리가 실행됩니다.
    }

    /**
     * 이메일 중복 검사
     */
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return userRepository.existsByEmailAndDeletedAtIsNull(email);
    }

    /**
     * 전화번호 중복 검사
     */
    @Transactional(readOnly = true)
    public boolean isPhoneNumberExists(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        List<String> encryptedPhoneNumbers = userRepository.findAllPhoneNumbersByDeletedAtIsNull();

        for (String encryptedPhone : encryptedPhoneNumbers) {
            try {
                String decryptedPhone = cryptoService.decryptAES(encryptedPhone);
                if (phoneNumber.equals(decryptedPhone)) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("전화번호 복호화 실패");
            }
        }
        return false;
    }
}