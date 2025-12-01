package com.example.d105.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

public class LoginResponse {

    @Getter
    @Builder
    public static class LoginResponseDTO {

        private String accessToken;
        private Long userId;

    }

}
