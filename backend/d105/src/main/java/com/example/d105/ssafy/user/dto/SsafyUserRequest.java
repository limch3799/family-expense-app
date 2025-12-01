package com.example.d105.ssafy.user.dto;

import com.example.d105.domain.user.dto.request.UserRequest;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SsafyUserRequest {
    // UserApiClient에서 사용하는 기본 필드들
    private String userId;
    private String apiKey;

    // 기존 내부 클래스들은 그대로 유지
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegistUser {
        private String userId;
        private String password;
        private String name;
        private String phone;

        public static RegistUser from(UserRequest.UserSignupRequest userDto) {
            return RegistUser.builder()
                    .userId(userDto.getEmail())
                    .password(userDto.getPassword())
                    .name(userDto.getUsername())
                    .phone(userDto.getPhoneNumber())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchUser {
        private String userId;
    }
}