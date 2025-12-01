package com.example.d105.domain.user.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetConfirmDto {
    private String email;
    private String verificationCode;
    private String newPassword;
}
