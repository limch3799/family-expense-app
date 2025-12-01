package com.example.d105.domain.user.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailConfirmDto {
    private String email;
    private String code;
}