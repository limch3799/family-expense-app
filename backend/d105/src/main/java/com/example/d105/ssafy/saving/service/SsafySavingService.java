package com.example.d105.ssafy.saving.service;

import com.example.d105.ssafy.saving.client.SavingClient;
import com.example.d105.ssafy.saving.dto.request.SavingRequest;
import com.example.d105.ssafy.saving.dto.response.SavingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SsafySavingService {

    private final SavingClient savingClient;

    public SavingResponse.CreateSavingResponse  createSavingProduct(SavingRequest.SubscriptionPeriod subscriptionPeriod)
    {
        return savingClient.createSavingResponse(subscriptionPeriod);
    }

    public SavingResponse.CreateSavingAccountResponse createSavingAccount(Long userId, String accountTypeUniqueNo, String depositBalance, String withdrawalAccountNo){
        return savingClient.createSavingAccountResponse(userId, accountTypeUniqueNo, depositBalance, withdrawalAccountNo);
    }

    //적금 계좌 조회 ( 단건)
    public SavingResponse.InquireAccountResponse inquireAccountResponse(Long userId, String accountNo){
        return savingClient.inquireAccountResponse(userId, accountNo);
    }

    //적금계좌 낭입 내역 조회
    public SavingResponse.PaymentResponse paymentResponse(Long userId, String accountNo){
        return savingClient.paymentResponse(userId, accountNo);
    }
}
