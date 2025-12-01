package com.example.d105.domain.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetRequestDto {
    private String username;
    private String phoneNumber;
    private String email;
}