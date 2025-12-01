package com.example.d105.domain.account.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardInfo {
    private Long cardId;
    private String cardNo;
    private String cardCompany;
    private Boolean isConnectedToGroup;
}