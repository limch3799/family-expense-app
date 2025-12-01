package com.example.d105.common.external.sms;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SmsSender {

    @Value("${coolsms.api.key}")
    private String apiKey;

    @Value("${coolsms.api.secret}")
    private String apiSecret;

    @Value("${coolsms.sms.sender-phone}")
    private String senderPhone;

    private DefaultMessageService messageService;

    @PostConstruct
    private void init() {
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    /**
     * SMS 인증 코드를 발송합니다.
     * @param to 수신자 전화번호
     * @param code 발송할 6자리 인증 코드
     */
    public void sendSms(String to, String code) {
        Message message = new Message();
        // 발신번호 및 수신번호는 "-"없이 숫자만 입력해주세요
        message.setFrom(senderPhone);
        message.setTo(to);
        message.setText("[가족 통장 서비스] 인증번호는 [" + code + "] 입니다.");

        try {
            this.messageService.sendOne(new SingleMessageSendingRequest(message));
            log.info("SMS 발송 성공: {}", to);
        } catch (Exception e) {
            log.error("SMS 발송 실패: {}", e.getMessage());
            // 실제 서비스에서는 예외 처리를 더 견고하게 해야 합니다.
        }
    }
}