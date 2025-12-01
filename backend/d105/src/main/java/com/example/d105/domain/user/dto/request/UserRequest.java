package com.example.d105.domain.user.dto.request;

import lombok.*;

public class UserRequest {

    @Data
    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSignupRequest{
        private String email;
        private String password;
        private String simplePassword;
        private String username;

        private String phoneNumber;
        private String birthDate;

        private String gender;
    }
}
