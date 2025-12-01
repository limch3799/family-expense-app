package com.example.d105.domain.user.dto.response;

import com.example.d105.domain.user.entity.User;
import com.example.d105.security.service.CryptoService;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserInfoResponseDto {

    private Long userId;
    private Long groupId;
    private String email; // API 규격서의 loginId에 해당
    private String username;
    private String phoneNumber;
    private String birthDate;
    private String gender;
    private Boolean planerPushEnabled;
    private Boolean reporterPushEnabled;
    private Boolean transactionPushEnabled;
    private String createdAt;

    // User 엔티티와 groupId, CryptoService를 받아 DTO로 변환하는 정적 메소드
    public static UserInfoResponseDto from(User user, Long groupId, CryptoService cryptoService) {
        return UserInfoResponseDto.builder()
                .userId(user.getUserId())
                .groupId(groupId) // 그룹 ID 설정
                .email(user.getEmail())
                // 암호화된 필드들은 cryptoService를 사용해 복호화
                .username(cryptoService.decryptAES(user.getUsername()))
                .phoneNumber(cryptoService.decryptAES(user.getPhoneNumber()))
                .birthDate(cryptoService.decryptAES(user.getBirthDate()))
                .gender(cryptoService.decryptAES(user.getGender()))
                .planerPushEnabled(user.getPlanerPushEnabled())
                .reporterPushEnabled(user.getReporterPushEnabled())
                .transactionPushEnabled(user.getTransactionPushEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}