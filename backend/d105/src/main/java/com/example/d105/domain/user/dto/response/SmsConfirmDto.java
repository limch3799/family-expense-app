package com.example.d105.domain.user.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsConfirmDto {
    private String phoneNumber;
    private String code;
}